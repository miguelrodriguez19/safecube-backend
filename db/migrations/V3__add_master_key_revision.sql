-- Add a server-owned revision for conditional master-wrapped KEK updates.

ALTER TABLE public.vault_key_material
    ADD COLUMN master_key_revision BIGINT NOT NULL DEFAULT 1;

ALTER TABLE public.vault_key_material
    ADD CONSTRAINT chk_vault_key_material_master_key_revision
        CHECK (master_key_revision > 0);
