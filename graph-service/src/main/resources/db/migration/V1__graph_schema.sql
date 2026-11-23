CREATE TABLE sync_state (id integer PRIMARY KEY CHECK(id=1), last_sequence bigint NOT NULL);
INSERT INTO sync_state VALUES (1,0);
CREATE TABLE graph_fact (
 fact_id uuid PRIMARY KEY, object_id varchar(40) NOT NULL, occurred_at timestamptz NOT NULL,
 event_type varchar(40) NOT NULL, severity varchar(20) NOT NULL, description varchar(500) NOT NULL,
 sensor_id varchar(80), latitude double precision NOT NULL, longitude double precision NOT NULL,
 created_at timestamptz NOT NULL, registered_at timestamptz NOT NULL, deleted_at timestamptz,
 active boolean NOT NULL, current_position integer
);
CREATE INDEX idx_graph_fact_object_order ON graph_fact(object_id,occurred_at,fact_id) WHERE active=true;
CREATE TABLE graph_version (version_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, recorded_at timestamptz NOT NULL, through_sequence bigint NOT NULL, source varchar(80) NOT NULL);
CREATE TABLE graph_node_snapshot (
 version_id bigint NOT NULL REFERENCES graph_version(version_id), fact_id uuid NOT NULL, object_id varchar(40) NOT NULL,
 occurred_at timestamptz NOT NULL, event_type varchar(40) NOT NULL, severity varchar(20) NOT NULL, description varchar(500) NOT NULL,
 sensor_id varchar(80), latitude double precision NOT NULL, longitude double precision NOT NULL,
 active boolean NOT NULL, position integer, PRIMARY KEY(version_id,fact_id)
);
CREATE TABLE graph_edge_snapshot (version_id bigint NOT NULL REFERENCES graph_version(version_id), source_fact_id uuid NOT NULL, target_fact_id uuid NOT NULL, edge_type varchar(40) NOT NULL, PRIMARY KEY(version_id,source_fact_id,target_fact_id,edge_type));
CREATE TABLE fact_position_history (id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, fact_id uuid NOT NULL, object_id varchar(40) NOT NULL, position integer NOT NULL, valid_from_version bigint NOT NULL REFERENCES graph_version(version_id), valid_to_version bigint REFERENCES graph_version(version_id), reason varchar(30) NOT NULL);
CREATE INDEX idx_position_lifecycle ON fact_position_history(fact_id,valid_from_version);
CREATE TABLE graph_change (id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, version_id bigint NOT NULL REFERENCES graph_version(version_id), source_sequence bigint NOT NULL, recorded_at timestamptz NOT NULL, change_type varchar(40) NOT NULL, fact_id uuid, object_id varchar(40), position integer, edge_type varchar(40), related_fact_id uuid);
CREATE INDEX idx_graph_change_version ON graph_change(version_id,id);
CREATE TABLE applied_change (sequence_id bigint PRIMARY KEY, fact_id uuid NOT NULL, operation varchar(12) NOT NULL, applied_at timestamptz NOT NULL);
