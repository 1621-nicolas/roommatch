-- Optimistic version: timestamps remain informational, never a concurrency token.
ALTER TABLE perfil_convivencia ADD version BIGINT NOT NULL CONSTRAINT DF_perfil_version DEFAULT 0;
