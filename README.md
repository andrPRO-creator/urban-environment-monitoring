# Мониторинг городской среды

Учебный проект по варианту 1. Fact Service генерирует события для объектов вымышленного города. Graph Service строит граф этих событий и хранит историю его изменений. Оба сервиса написаны на Java 21 и Spring Boot и работают каждый со своей базой PostgreSQL 16.

## Запуск через Docker

1. Запустите Docker Desktop в режиме Linux-контейнеров. Проверьте, что `docker ps` выполняется без ошибки.
2. Откройте PowerShell в корне проекта, где находится `docker-compose.yml`, и выполните:

   ```powershell
   if (-not (Test-Path .env)) { Copy-Item .env.example .env }
   docker compose up --build -d
   docker compose ps
   ```

3. Дождитесь запуска приложений. Для просмотра журналов выполните `docker compose logs -f fact-service graph-service`. После сообщений `Started FactServiceApplication` и `Started GraphServiceApplication` нажмите Ctrl+C для выхода из просмотра журналов.

| Сервис | Swagger UI | OpenAPI JSON | PostgreSQL с хоста |
|---|---|---|---|
| Fact Service | [localhost:8081](http://localhost:8081/swagger-ui.html) | [Спецификация](http://localhost:8081/v3/api-docs) | `localhost:5433/fact_db` |
| Graph Service | [localhost:8082](http://localhost:8082/swagger-ui.html) | [Спецификация](http://localhost:8082/v3/api-docs) | `localhost:5434/graph_db` |

Flyway создаёт таблицы и загружает шесть городских объектов. Для подключения к учебным БД по умолчанию используются `postgres` и пароль `root`. Если порт занят, измените соответствующий параметр в `.env`: `FACT_SERVICE_PORT`, `GRAPH_SERVICE_PORT`, `FACT_DB_PORT` или `GRAPH_DB_PORT`. После смены HTTP-порта подставьте его в адреса Swagger и команды ниже.

При первой сборке Maven загружает зависимости. На шаге `dependency:go-offline` вывод может некоторое время не меняться из-за тихого режима Maven. При ошибке подключения к `dockerDesktopLinuxEngine` проверьте, что Docker Desktop запущен и `docker ps` видит движок.

Команда `docker compose down` останавливает проект и сохраняет данные в томах PostgreSQL.

## Первый запрос и результат

В Swagger Fact Service откройте `POST /api/v1/generator/run-once`, нажмите **Try it out**, затем **Execute**. Тело запроса не требуется. В ответе `added` и `deleted` показывают размер изменения, а `throughSequence` — последний номер в журнале. На пустой базе при настройках по умолчанию первый пакет добавляет пять фактов. При последующих вызовах генератор может добавлять или удалять факты.

В Swagger Graph Service вызовите `GET /api/v1/graph`. В ответе `nodes` — активные факты, `edges` — связи между ними, `versionId` — версия графа. После успешной синхронизации `lastSequence` совпадает с `throughSequence` последнего пакета, если новый пакет ещё не обработан.

Тот же сценарий в PowerShell. Идентификаторы объекта и факта берутся из ответа графа:

```powershell
$factApi = 'http://localhost:8081/api/v1'
$graphApi = 'http://localhost:8082/api/v1'

Invoke-RestMethod "$factApi/objects"
$batch = Invoke-RestMethod -Method Post "$factApi/generator/run-once"
$batch

$graph = Invoke-RestMethod "$graphApi/graph"
$graph | ConvertTo-Json -Depth 10

if ($graph.nodes.Count -gt 0) {
    $node = $graph.nodes[0]
    $day = ([DateTime]::UtcNow).ToString('yyyy-MM-dd')
    Invoke-RestMethod "$graphApi/graph/objects/$($node.objectId)/chain?asOf=$day"
    Invoke-RestMethod "$graphApi/facts/$($node.factId)/lifecycle"
}

Invoke-RestMethod "$graphApi/graph/changes?page=0&size=20"
Invoke-RestMethod "$graphApi/graph/versions"
```

## Запросы и автоматическая генерация

| Запрос к Graph Service | Что возвращает |
|---|---|
| `GET /api/v1/graph?edgeType=NEXT_FOR_OBJECT` | Активные факты и рёбра указанного типа. Без фильтра возвращаются оба типа рёбер. |
| `GET /api/v1/graph/objects/{objectId}/chain?asOf=YYYY-MM-DD` | Цепочку объекта в последней версии, записанной до начала следующего дня по UTC. |
| `GET /api/v1/facts/{factId}/lifecycle` | Регистрацию факта с первой позицией, последующие изменения позиции и удаление, если оно произошло. |
| `GET /api/v1/graph/changes?page=0&size=20` | Страницу журнала со сведениями об изменениях узлов, рёбер и позиций. |
| `GET /api/v1/graph/versions/{versionId}` | Снимок конкретной версии. Идентификатор можно взять из `GET /api/v1/graph/versions`. |

В Fact Service вызовите `POST /api/v1/generator/start` для генерации пакетов с интервалом от 5 до 15 секунд по умолчанию. Через несколько пакетов вызовите `POST /api/v1/generator/stop`. Чтобы увидеть результат удаления, найдите факт через `GET /api/v1/facts?deleted=true` и проверьте его жизненный цикл в Graph Service. Для сравнения состояний одного дня используйте номера версий.

## Как устроен проект

- [Fact Service](fact-service/src/main) хранит городские объекты, факты и журнал `fact_change`. После фиксации пакета сервис через `RestTemplate` отправляет уведомление на `POST /api/v1/synchronization/notifications` с полями `source`, `throughSequence` и `notifiedAt`. Удаление факта помечается датой в базе.
- [Graph Service](graph-service/src/main) через `RestTemplate` запрашивает журнал на `GET /internal/v1/changes` от своего курсора до `throughSequence`. Сервис применяет изменения по порядку и сохраняет курсор, версию, снимки узлов и рёбер, историю позиций и журнал изменений в своей базе. Уже обработанные номера пропускаются.
- Узел графа соответствует факту. Ребро `NEXT_FOR_OBJECT` связывает соседние активные факты одного объекта по времени события, при совпадении используется UUID. Ребро `NEARBY_IN_TIME` соединяет факты разных объектов, если разница во времени не превышает 30 минут, а расстояние по формуле гаверсинусов не превышает 500 метров. При удалении факта граф перестраивается, а старые версии сохраняются.

Пороги близости и настройки генератора можно менять в `.env`. Доставка уведомлений синхронная. Автоматические повторы после сетевого сбоя не предусмотрены.

## Запуск из IDE

Запустите только базы командой `docker compose up -d fact-db graph-db`. Импортируйте `fact-service/pom.xml` и `graph-service/pom.xml` как отдельные Maven-проекты с Java 21. Запустите `FactServiceApplication` и `GraphServiceApplication`. При стандартных портах приложения используют адреса из таблицы выше. После изменения портов БД укажите `DB_URL` отдельно для каждого приложения. Если вы поменяли учётные данные, задайте `DB_USERNAME` и `DB_PASSWORD` в конфигурациях запуска.
