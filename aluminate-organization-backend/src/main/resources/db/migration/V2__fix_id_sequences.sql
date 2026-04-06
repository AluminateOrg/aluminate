-- admin
CREATE SEQUENCE IF NOT EXISTS admin_id_seq START 1;
ALTER TABLE admin ALTER COLUMN id SET DEFAULT nextval('admin_id_seq');

-- member
CREATE SEQUENCE IF NOT EXISTS member_id_seq START 1;
ALTER TABLE member ALTER COLUMN id SET DEFAULT nextval('member_id_seq');

-- organization
CREATE SEQUENCE IF NOT EXISTS organization_id_seq START 1;
ALTER TABLE organization ALTER COLUMN id SET DEFAULT nextval('organization_id_seq');

-- campaign
CREATE SEQUENCE IF NOT EXISTS campaign_id_seq START 1;
ALTER TABLE campaign ALTER COLUMN id SET DEFAULT nextval('campaign_id_seq');

-- event
CREATE SEQUENCE IF NOT EXISTS event_id_seq START 1;
ALTER TABLE event ALTER COLUMN id SET DEFAULT nextval('event_id_seq');

-- groups
CREATE SEQUENCE IF NOT EXISTS groups_id_seq START 1;
ALTER TABLE groups ALTER COLUMN id SET DEFAULT nextval('groups_id_seq');

-- donation
CREATE SEQUENCE IF NOT EXISTS donation_id_seq START 1;
ALTER TABLE donation ALTER COLUMN id SET DEFAULT nextval('donation_id_seq');

-- payment
CREATE SEQUENCE IF NOT EXISTS payment_id_seq START 1;
ALTER TABLE payment ALTER COLUMN id SET DEFAULT nextval('payment_id_seq');

-- transaction
CREATE SEQUENCE IF NOT EXISTS transaction_id_seq START 1;
ALTER TABLE transaction ALTER COLUMN id SET DEFAULT nextval('transaction_id_seq');

-- notification
CREATE SEQUENCE IF NOT EXISTS notification_id_seq START 1;
ALTER TABLE notification ALTER COLUMN id SET DEFAULT nextval('notification_id_seq');

-- inapp_notification
CREATE SEQUENCE IF NOT EXISTS inapp_notification_id_seq START 1;
ALTER TABLE inapp_notification ALTER COLUMN id SET DEFAULT nextval('inapp_notification_id_seq');

-- mentor
CREATE SEQUENCE IF NOT EXISTS mentor_id_seq START 1;
ALTER TABLE mentor ALTER COLUMN id SET DEFAULT nextval('mentor_id_seq');

-- mentor_program
CREATE SEQUENCE IF NOT EXISTS mentor_program_id_seq START 1;
ALTER TABLE mentor_program ALTER COLUMN id SET DEFAULT nextval('mentor_program_id_seq');

-- member_event
CREATE SEQUENCE IF NOT EXISTS member_event_id_seq START 1;
ALTER TABLE member_event ALTER COLUMN id SET DEFAULT nextval('member_event_id_seq');

-- member_group
CREATE SEQUENCE IF NOT EXISTS member_group_id_seq START 1;
ALTER TABLE member_group ALTER COLUMN id SET DEFAULT nextval('member_group_id_seq');

-- payment_option
CREATE SEQUENCE IF NOT EXISTS payment_option_id_seq START 1;
ALTER TABLE payment_option ALTER COLUMN id SET DEFAULT nextval('payment_option_id_seq');

-- ack_transaction
CREATE SEQUENCE IF NOT EXISTS ack_transaction_id_seq START 1;
ALTER TABLE ack_transaction ALTER COLUMN id SET DEFAULT nextval('ack_transaction_id_seq');

-- staged_transaction
CREATE SEQUENCE IF NOT EXISTS staged_transaction_id_seq START 1;
ALTER TABLE staged_transaction ALTER COLUMN id SET DEFAULT nextval('staged_transaction_id_seq');

-- incoming_transaction
CREATE SEQUENCE IF NOT EXISTS incoming_transaction_id_seq START 1;
ALTER TABLE incoming_transaction ALTER COLUMN id SET DEFAULT nextval('incoming_transaction_id_seq');

-- global_transaction_ticket
CREATE SEQUENCE IF NOT EXISTS global_transaction_ticket_id_seq START 1;
ALTER TABLE global_transaction_ticket ALTER COLUMN id SET DEFAULT nextval('global_transaction_ticket_id_seq');

-- organization_billing
CREATE SEQUENCE IF NOT EXISTS organization_billing_id_seq START 1;
ALTER TABLE organization_billing ALTER COLUMN id SET DEFAULT nextval('organization_billing_id_seq');

-- organization_billing_invoice
CREATE SEQUENCE IF NOT EXISTS organization_billing_invoice_id_seq START 1;
ALTER TABLE organization_billing_invoice ALTER COLUMN id SET DEFAULT nextval('organization_billing_invoice_id_seq');

-- organization_notification_settings
CREATE SEQUENCE IF NOT EXISTS org_notification_settings_id_seq START 1;
ALTER TABLE organization_notification_settings ALTER COLUMN id SET DEFAULT nextval('org_notification_settings_id_seq');

-- organization_privacy_settings
CREATE SEQUENCE IF NOT EXISTS org_privacy_settings_id_seq START 1;
ALTER TABLE organization_privacy_settings ALTER COLUMN id SET DEFAULT nextval('org_privacy_settings_id_seq');

-- organization_settings
CREATE SEQUENCE IF NOT EXISTS organization_settings_id_seq START 1;
ALTER TABLE organization_settings ALTER COLUMN id SET DEFAULT nextval('organization_settings_id_seq');