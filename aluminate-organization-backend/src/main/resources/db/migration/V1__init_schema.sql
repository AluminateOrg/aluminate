-- -------------------------------------------------------------
-- TablePlus 6.7.8(650)
--
-- https://tableplus.com/
--
-- Database: aluminatedb
-- Generation Time: 2026-01-02 20:57:28.9480
-- -------------------------------------------------------------


-- Table Definition
CREATE TABLE "public"."campaign" (
                                     "current_amount" numeric(15,2),
                                     "end_date" date NOT NULL,
                                     "is_active" bool,
                                     "is_deleted" bool,
                                     "start_date" date,
                                     "target" numeric(15,2) NOT NULL,
                                     "total_donors" int4 CHECK (total_donors >= 0),
                                     "created_at" timestamp,
                                     "deleted_at" timestamp,
                                     "id" int8 NOT NULL,
                                     "updated_at" timestamp,
                                     "title" varchar(100) NOT NULL,
                                     "description" varchar(1000),
                                     "type" varchar(255) NOT NULL CHECK ((type)::text = ANY ((ARRAY['FUNDRAISING'::character varying, 'EMERGENCY'::character varying, 'GENERAL'::character varying, 'SCHOLARSHIP'::character varying, 'INFRASTRUCTURE'::character varying, 'OTHER'::character varying])::text[])),
    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."event" (
                                  "current_participants" int4 NOT NULL,
                                  "end_date" date,
                                  "end_time" time,
                                  "is_deleted" bool NOT NULL,
                                  "is_public" bool,
                                  "max_participants" int4 NOT NULL,
                                  "price" numeric(38,2),
                                  "registration_deadline" date,
                                  "requires_approval" bool NOT NULL,
                                  "start_date" date,
                                  "start_time" time,
                                  "deleted_at" timestamp,
                                  "id" int8 NOT NULL,
                                  "description" varchar(255),
                                  "location" varchar(255),
                                  "status" varchar(255) NOT NULL CHECK ((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])),
    "title" varchar(255),
    "type" varchar(255) NOT NULL CHECK ((type)::text = ANY ((ARRAY['SOCIAL'::character varying, 'WORKSHOP'::character varying, 'MEETING'::character varying, 'FUNDRAISING'::character varying, 'NETWORKING'::character varying, 'WEBINAR'::character varying])::text[])),
    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."admin" (
                                  "email_verified" bool NOT NULL,
                                  "id" int8 NOT NULL,
                                  "email" varchar(255),
                                  "name" varchar(255),
                                  "password" varchar(255),
                                  "phone" varchar(255),
                                  PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."ack_transaction" (
                                            "amount" numeric(38,2) NOT NULL,
                                            "created_at" timestamp,
                                            "id" int8 NOT NULL,
                                            "incoming_transaction_id" int8 NOT NULL,
                                            "category" varchar(255) CHECK ((category)::text = ANY ((ARRAY['MEMBERSHIP'::character varying, 'DONATION'::character varying, 'MENTORSHIP'::character varying, 'EVENT'::character varying, 'MATERIAL'::character varying])::text[])),
    "currency" varchar(255) NOT NULL,
    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."donation" (
                                     "amount" numeric(10,2) NOT NULL,
                                     "date" date NOT NULL,
                                     "donation_date" date,
                                     "is_anonymous" bool NOT NULL,
                                     "campaign_id" int8 NOT NULL,
                                     "created_at" timestamp NOT NULL,
                                     "id" int8 NOT NULL,
                                     "member_id" int8 NOT NULL,
                                     "transaction_id" int8 NOT NULL,
                                     "updated_at" timestamp,
                                     "payment_reference" varchar(100),
                                     "message" varchar(500),
                                     "payment_method" varchar(255),
                                     "payment_status" varchar(255),
                                     "status" varchar(255),
                                     PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."inapp_notification" (
                                               "read_flag" bool NOT NULL,
                                               "created_at" timestamptz NOT NULL,
                                               "id" int8 NOT NULL,
                                               "member_id" varchar(255) NOT NULL,
                                               "payload_json" varchar(255) NOT NULL,
                                               "tenant_id" varchar(255) NOT NULL,
                                               "type" varchar(255) NOT NULL,
                                               PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."groups" (
                                   "created_date" date,
                                   "current_members" int4 NOT NULL,
                                   "is_active" bool NOT NULL,
                                   "is_deleted" bool NOT NULL,
                                   "max_members" int4 NOT NULL,
                                   "required_approval" bool NOT NULL,
                                   "deleted_at" timestamp,
                                   "id" int8 NOT NULL,
                                   "category" varchar(255) CHECK ((category)::text = ANY ((ARRAY['PROFESSIONAL'::character varying, 'SOCIAL'::character varying, 'HOBBY'::character varying, 'ACADEMIC'::character varying])::text[])),
    "description" varchar(255) NOT NULL,
    "name" varchar(255) NOT NULL,
    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."member" (
                                   "batch" int4 NOT NULL,
                                   "is_active" bool NOT NULL,
                                   "is_membership_paid" bool NOT NULL,
                                   "public_profile_enabled" bool,
                                   "id" int8 NOT NULL,
                                   "organization_id" int8,
                                   "address" varchar(255),
                                   "company" varchar(255),
                                   "degree" varchar(255),
                                   "email" varchar(255),
                                   "github_url" varchar(255),
                                   "linkedin_url" varchar(255),
                                   "name" varchar(255),
                                   "nic" varchar(255),
                                   "password" varchar(255),
                                   "phone" varchar(255),
                                   "photo_url" varchar(255),
                                   "position" varchar(255),
                                   "public_slug" varchar(255),
                                   "reg_no" varchar(255),
                                   "website_url" varchar(255),
                                   PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."member_event" (
                                         "is_attending" bool NOT NULL,
                                         "setrsvp" bool NOT NULL,
                                         "event_id" int8 NOT NULL,
                                         "id" int8 NOT NULL,
                                         "member_id" int8 NOT NULL,
                                         PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."payment_option" (
                                           "id" int8 NOT NULL,
                                           "card_holder_name" varchar(255),
                                           "card_type" varchar(255) CHECK ((card_type)::text = ANY ((ARRAY['VISA'::character varying, 'MASTERCARD'::character varying, 'AMEX'::character varying])::text[])),
    "created_at" timestamp NOT NULL,
    "expiry_month" int4,
    "expiry_year" int4,
    "is_active" bool NOT NULL,
    "is_default" bool NOT NULL,
    "last_four_digits" varchar(4),
    "payment_token" varchar(255) NOT NULL,
    "member_id" int8 NOT NULL,
    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."mentor_languages" (
                                             "mentor_id" int8 NOT NULL,
                                             "language" varchar(255)
);

-- Table Definition
CREATE TABLE "public"."notification" (
                                         "is_read" bool NOT NULL,
                                         "date" timestamp,
                                         "id" int8 NOT NULL,
                                         "member_id" int8 NOT NULL,
                                         "message" varchar(255),
                                         "title" varchar(255),
                                         "type" varchar(255) NOT NULL CHECK ((type)::text = ANY ((ARRAY['EVENT'::character varying, 'GROUP'::character varying, 'DONATION'::character varying, 'SYSTEM'::character varying, 'INFO'::character varying, 'MENTORSHIP'::character varying, 'WARNING'::character varying, 'SUCCESS'::character varying])::text[])),
    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."organization" (
                                         "current_member_count" int4 NOT NULL,
                                         "is_deleted" bool NOT NULL,
                                         "is_membership_free" bool NOT NULL,
                                         "max_member_count" int4 NOT NULL,
                                         "admin_id" int8,
                                         "id" int8 NOT NULL,
                                         "organization_name" varchar(255) NOT NULL,
                                         "status" varchar(255) CHECK ((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'SUSPENDED'::character varying])::text[])),
    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."member_group" (
                                         "is_approved" bool NOT NULL,
                                         "group_id" int8 NOT NULL,
                                         "id" int8 NOT NULL,
                                         "member_id" int8,
                                         "request_date" timestamp,
                                         "response_date" timestamp,
                                         "request_status" varchar(255) CHECK ((request_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])),
    "role" varchar(255),
    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."mentor" (
                                   "hourly_rate" numeric(38,2),
                                   "is_approved" bool NOT NULL,
                                   "max_mentees" int4 NOT NULL,
                                   "rating" float8,
                                   "session_count" int4 NOT NULL,
                                   "years_of_experience" int4 NOT NULL,
                                   "created_at" timestamp,
                                   "id" int8 NOT NULL,
                                   "member_id" int8,
                                   "availability" varchar(255),
                                   "bio" varchar(255),
                                   "linked_in_url" varchar(255),
                                   "motivation" varchar(255),
                                   "portfolio_url" varchar(255),
                                   "preferred_mentee_level" varchar(255),
                                   "status" varchar(255),
                                   PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."mentor_skills" (
                                          "mentor_id" int8 NOT NULL,
                                          "skill" varchar(255)
);

-- Table Definition
CREATE TABLE "public"."mentor_program" (
                                           "date" date,
                                           "is_paid" bool NOT NULL,
                                           "time" time,
                                           "created_at" timestamp,
                                           "created_by" int8 NOT NULL,
                                           "id" int8 NOT NULL,
                                           "mentor_id" int8 NOT NULL,
                                           "payment_id" int8,
                                           "program_url" varchar(255),
                                           "status" varchar(255),
                                           PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."organization_billing" (
                                                 "member_limit" int4 NOT NULL,
                                                 "next_billing_date" date,
                                                 "id" int8 NOT NULL,
                                                 "billing_address" varchar(255),
                                                 "billing_city" varchar(255),
                                                 "billing_company_name" varchar(255),
                                                 "billing_country" varchar(255),
                                                 "billing_email" varchar(255),
                                                 "billing_phone" varchar(255),
                                                 "billing_state" varchar(255),
                                                 "billing_zip" varchar(255),
                                                 "tier" varchar(255),
                                                 PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."organization_billing_invoice" (
                                                         "amount" numeric(38,2),
                                                         "date" date,
                                                         "id" int8 NOT NULL,
                                                         "invoice_url" varchar(255),
                                                         PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."organization_notification_settings" (
                                                               "donation_alert_notifications" bool NOT NULL,
                                                               "email_notifications" bool NOT NULL,
                                                               "event_reminder_notifications" bool NOT NULL,
                                                               "member_join_notifications" bool NOT NULL,
                                                               "system_update_notifications" bool NOT NULL,
                                                               "weekly_report_notifications" bool NOT NULL,
                                                               "id" int8 NOT NULL,
                                                               PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."organization_privacy_settings" (
                                                          "allow_guest_events" bool NOT NULL,
                                                          "allow_member_search" bool NOT NULL,
                                                          "show_member_count" bool NOT NULL,
                                                          "id" int8 NOT NULL,
                                                          PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."organization_settings" (
                                                  "id" int8 NOT NULL,
                                                  "address" varchar(255),
                                                  "description" varchar(255),
                                                  "logo_url" varchar(255),
                                                  "name" varchar(255),
                                                  "organization_email" varchar(255),
                                                  "phone_number" varchar(255),
                                                  "portal_url" varchar(255),
                                                  PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."transaction" (
                                        "amount" numeric(38,2) NOT NULL,
                                        "created_at" timestamp,
                                        "id" int8 NOT NULL,
                                        "member_id" int8,
                                        "organization_id" int8 NOT NULL,
                                        "card_expiry" varchar(255),
                                        "card_holder_name" varchar(255),
                                        "card_no" varchar(255),
                                        "category" varchar(255) CHECK ((category)::text = ANY ((ARRAY['MEMBERSHIP'::character varying, 'DONATION'::character varying, 'MENTORSHIP'::character varying, 'EVENT'::character varying, 'MATERIAL'::character varying])::text[])),
    "currency" varchar(255) NOT NULL,
    "method" varchar(255),
    "payment_id" varchar(255),
    "status_code" varchar(255),
    "status_message" varchar(255),
    "transaction_status" varchar(255) CHECK ((transaction_status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('SUCCESS'::character varying)::text, ('FAILED'::character varying)::text, ('COMPLETED'::character varying)::text, ('CANCELLED'::character varying)::text])),
    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."payment" (
                                    "amount" float8,
                                    "id" int8 NOT NULL,
                                    "payement_date" timestamp,
                                    "payer_id" int8,
                                    "payment_method" varchar(255),
                                    "status" varchar(255),
                                    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."program_participants" (
                                                 "member_id" int8 NOT NULL,
                                                 "program_id" int8 NOT NULL,
                                                 PRIMARY KEY ("member_id","program_id")
);

-- Table Definition
CREATE TABLE "public"."staged_transaction" (
                                               "amount" numeric(38,2) NOT NULL,
                                               "created_at" timestamp,
                                               "global_transaction_ticket_id" int8,
                                               "id" int8 NOT NULL,
                                               "incoming_transaction_id" int8 NOT NULL,
                                               "organization_id" int8 NOT NULL,
                                               "category" varchar(255) CHECK ((category)::text = ANY ((ARRAY['MEMBERSHIP'::character varying, 'DONATION'::character varying, 'MENTORSHIP'::character varying, 'EVENT'::character varying, 'MATERIAL'::character varying])::text[])),
    "currency" varchar(255) NOT NULL,
    "key" varchar(255),
    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."incoming_transaction" (
                                                 "ack" bool,
                                                 "amount" numeric(38,2) NOT NULL,
                                                 "staged" bool,
                                                 "created_at" timestamp,
                                                 "id" int8 NOT NULL,
                                                 "organization_id" int8 NOT NULL,
                                                 "transaction_id" int8 NOT NULL,
                                                 "category" varchar(255) CHECK ((category)::text = ANY ((ARRAY['MEMBERSHIP'::character varying, 'DONATION'::character varying, 'MENTORSHIP'::character varying, 'EVENT'::character varying, 'MATERIAL'::character varying])::text[])),
    "currency" varchar(255) NOT NULL,
    "transaction_status" varchar(255) CHECK ((transaction_status)::text = ANY (ARRAY[('PENDING'::character varying)::text, ('SUCCESS'::character varying)::text, ('FAILED'::character varying)::text, ('COMPLETED'::character varying)::text, ('CANCELLED'::character varying)::text])),
    PRIMARY KEY ("id")
);

-- Table Definition
CREATE TABLE "public"."global_transaction_ticket" (
                                                      "ack" bool,
                                                      "amount" numeric(38,2),
                                                      "staged" bool,
                                                      "created_at" timestamp,
                                                      "id" int8 NOT NULL,
                                                      "organization_id" int8,
                                                      "key" varchar(255) NOT NULL,
                                                      "send" bool,
                                                      "status" varchar(255) CHECK ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying, 'PAID'::character varying, 'REJECTED'::character varying, 'COMPLETED'::character varying])::text[])),
    PRIMARY KEY ("id")
);

ALTER TABLE "public"."ack_transaction" ADD FOREIGN KEY ("incoming_transaction_id") REFERENCES "public"."incoming_transaction"("id") ON DELETE CASCADE ON UPDATE CASCADE;


-- Indices
CREATE UNIQUE INDEX ack_transaction_incoming_transaction_id_key ON public.ack_transaction USING btree (incoming_transaction_id);
ALTER TABLE "public"."donation" ADD FOREIGN KEY ("member_id") REFERENCES "public"."member"("id");
ALTER TABLE "public"."donation" ADD FOREIGN KEY ("campaign_id") REFERENCES "public"."campaign"("id");
ALTER TABLE "public"."donation" ADD FOREIGN KEY ("transaction_id") REFERENCES "public"."transaction"("id") ON DELETE CASCADE ON UPDATE CASCADE;


-- Indices
CREATE UNIQUE INDEX donation_transaction_id_key ON public.donation USING btree (transaction_id);


-- Indices
CREATE UNIQUE INDEX groups_name_key ON public.groups USING btree (name);
ALTER TABLE "public"."member" ADD FOREIGN KEY ("organization_id") REFERENCES "public"."organization"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."member_event" ADD FOREIGN KEY ("event_id") REFERENCES "public"."event"("id");
ALTER TABLE "public"."member_event" ADD FOREIGN KEY ("member_id") REFERENCES "public"."member"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mentor_languages" ADD FOREIGN KEY ("mentor_id") REFERENCES "public"."mentor"("id");
ALTER TABLE "public"."notification" ADD FOREIGN KEY ("member_id") REFERENCES "public"."member"("id");
ALTER TABLE "public"."organization" ADD FOREIGN KEY ("admin_id") REFERENCES "public"."admin"("id") ON DELETE CASCADE ON UPDATE CASCADE;


-- Indices
CREATE UNIQUE INDEX organization_admin_id_key ON public.organization USING btree (admin_id);
CREATE UNIQUE INDEX organization_organization_name_key ON public.organization USING btree (organization_name);
ALTER TABLE "public"."member_group" ADD FOREIGN KEY ("member_id") REFERENCES "public"."member"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."member_group" ADD FOREIGN KEY ("group_id") REFERENCES "public"."groups"("id");
ALTER TABLE "public"."mentor" ADD FOREIGN KEY ("member_id") REFERENCES "public"."member"("id") ON DELETE CASCADE ON UPDATE CASCADE;


-- Indices
CREATE UNIQUE INDEX mentor_member_id_key ON public.mentor USING btree (member_id);
ALTER TABLE "public"."mentor_skills" ADD FOREIGN KEY ("mentor_id") REFERENCES "public"."mentor"("id");
ALTER TABLE "public"."mentor_program" ADD FOREIGN KEY ("created_by") REFERENCES "public"."member"("id");
ALTER TABLE "public"."mentor_program" ADD FOREIGN KEY ("mentor_id") REFERENCES "public"."mentor"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mentor_program" ADD FOREIGN KEY ("payment_id") REFERENCES "public"."payment"("id");


-- Indices
CREATE UNIQUE INDEX mentor_program_payment_id_key ON public.mentor_program USING btree (payment_id);
ALTER TABLE "public"."transaction" ADD FOREIGN KEY ("member_id") REFERENCES "public"."member"("id") ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE "public"."transaction" ADD FOREIGN KEY ("organization_id") REFERENCES "public"."organization"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."payment" ADD FOREIGN KEY ("payer_id") REFERENCES "public"."member"("id");


-- Indices
CREATE UNIQUE INDEX payment_payer_id_key ON public.payment USING btree (payer_id);
ALTER TABLE "public"."program_participants" ADD FOREIGN KEY ("member_id") REFERENCES "public"."member"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."program_participants" ADD FOREIGN KEY ("program_id") REFERENCES "public"."mentor_program"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."staged_transaction" ADD FOREIGN KEY ("incoming_transaction_id") REFERENCES "public"."incoming_transaction"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."staged_transaction" ADD FOREIGN KEY ("organization_id") REFERENCES "public"."organization"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."staged_transaction" ADD FOREIGN KEY ("global_transaction_ticket_id") REFERENCES "public"."global_transaction_ticket"("id") ON DELETE SET NULL ON UPDATE CASCADE;


-- Indices
CREATE UNIQUE INDEX staged_transaction_incoming_transaction_id_key ON public.staged_transaction USING btree (incoming_transaction_id);
ALTER TABLE "public"."incoming_transaction" ADD FOREIGN KEY ("transaction_id") REFERENCES "public"."transaction"("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."incoming_transaction" ADD FOREIGN KEY ("organization_id") REFERENCES "public"."organization"("id") ON DELETE CASCADE ON UPDATE CASCADE;


-- Indices
CREATE UNIQUE INDEX incoming_transaction_transaction_id_key ON public.incoming_transaction USING btree (transaction_id);
ALTER TABLE "public"."global_transaction_ticket" ADD FOREIGN KEY ("organization_id") REFERENCES "public"."organization"("id") ON DELETE CASCADE ON UPDATE CASCADE;


-- Indices
CREATE UNIQUE INDEX global_transaction_ticket_key_key ON public.global_transaction_ticket USING btree (key);
