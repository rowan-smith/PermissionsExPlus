CREATE TABLE IF NOT EXISTS user_group_memberships (
    user_id TEXT NOT NULL,
    group_id INT NOT NULL,
    context_key TEXT NOT NULL DEFAULT '',
    expires_at TIMESTAMP NULL,
    PRIMARY KEY (user_id, group_id, context_key),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES "groups"(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_group_memberships_user ON user_group_memberships(user_id);

CREATE TABLE IF NOT EXISTS world_inheritance (
    world TEXT NOT NULL,
    position INT NOT NULL,
    inherited_world TEXT NOT NULL,
    PRIMARY KEY (world, position)
);

CREATE TABLE IF NOT EXISTS user_entity_options (
    user_id TEXT NOT NULL,
    option_key TEXT NOT NULL,
    option_value TEXT,
    context_key TEXT,
    PRIMARY KEY (user_id, option_key, context_key),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS group_entity_options (
    group_id INT NOT NULL,
    option_key TEXT NOT NULL,
    option_value TEXT,
    context_key TEXT,
    PRIMARY KEY (group_id, option_key, context_key),
    FOREIGN KEY (group_id) REFERENCES "groups"(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_world_inheritance_world ON world_inheritance(world);
CREATE INDEX IF NOT EXISTS idx_user_entity_options_user ON user_entity_options(user_id);
CREATE INDEX IF NOT EXISTS idx_group_entity_options_group ON group_entity_options(group_id);
