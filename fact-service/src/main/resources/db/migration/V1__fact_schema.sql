CREATE TABLE monitored_object (
  object_id varchar(40) PRIMARY KEY, name varchar(120) NOT NULL, object_type varchar(40) NOT NULL,
  latitude double precision NOT NULL, longitude double precision NOT NULL, description varchar(500) NOT NULL
);
INSERT INTO monitored_object VALUES
('cross-north-01','Северный перекрёсток','INTERSECTION',55.75120,37.61010,'Синтетический перекрёсток у Северной площади'),
('road-river-02','Набережная река','ROAD_SECTION',55.75200,37.61100,'Участок улицы вдоль реки'),
('bridge-blue-03','Голубой мост','BRIDGE',55.75310,37.61200,'Городской мост через канал'),
('stop-market-04','Остановка Рынок','BUS_STOP',55.75070,37.60920,'Остановка у вымышленного рынка'),
('sensor-park-05','Датчик Парковый','SENSOR',55.75410,37.61320,'Стационарный датчик городской среды'),
('road-school-06','Школьный проезд','ROAD_SECTION',55.74980,37.60850,'Улица рядом с учебным кварталом');
CREATE TABLE fact (
  fact_id uuid PRIMARY KEY, object_id varchar(40) NOT NULL REFERENCES monitored_object(object_id),
  occurred_at timestamptz NOT NULL, event_type varchar(40) NOT NULL, severity varchar(20) NOT NULL,
  description varchar(500) NOT NULL, sensor_id varchar(80), latitude double precision NOT NULL,
  longitude double precision NOT NULL, created_at timestamptz NOT NULL, deleted_at timestamptz
);
CREATE INDEX idx_fact_object_time ON fact(object_id, occurred_at, fact_id);
CREATE INDEX idx_fact_deleted ON fact(deleted_at);
CREATE TABLE fact_change (
  sequence_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, fact_id uuid NOT NULL, operation varchar(12) NOT NULL,
  operated_at timestamptz NOT NULL, object_id varchar(40) NOT NULL, occurred_at timestamptz NOT NULL,
  event_type varchar(40) NOT NULL, severity varchar(20) NOT NULL, description varchar(500) NOT NULL,
  sensor_id varchar(80), latitude double precision NOT NULL, longitude double precision NOT NULL,
  created_at timestamptz NOT NULL, deleted_at timestamptz
);
CREATE INDEX idx_change_sequence ON fact_change(sequence_id);
