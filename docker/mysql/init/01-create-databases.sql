-- Creates the per-service databases used by the Sewage Alert Hyderabad
-- microservices. Mounted into /docker-entrypoint-initdb.d so MySQL creates
-- them on first boot. Tables are auto-created by Hibernate (ddl-auto: update).

CREATE DATABASE IF NOT EXISTS sewagealert_auth;
CREATE DATABASE IF NOT EXISTS sewagealert_users;
CREATE DATABASE IF NOT EXISTS sewagealert_complaints;
CREATE DATABASE IF NOT EXISTS sewagealert_community;
CREATE DATABASE IF NOT EXISTS sewagealert_notifications;
