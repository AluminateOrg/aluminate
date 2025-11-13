# Transaction Management – Organization System

The Organization System handles financial activities internally (donations, memberships, mentorships, events, merchandise sales), but **all payments are processed through the Global Payment Gateway**.  
Therefore, the organization must periodically **report and reconcile** its internal transaction records with the Global Server.  
This is done using a **staging and ticketing mechanism**.

---

## Tables Involved

| Table | Role (Short Description) |
|------|---------------------------|
| **incoming_transaction** | Stores every payment made inside the organization system. |
| **staged_transaction** | Stores incoming transactions that are pending reconciliation. |
| **global_transaction_ticket** | Represents a batch of staged transactions to be settled by the Global Server. |
| **ack_transaction** | Stores finalized transactions that have been acknowledged (paid out) by the Global Server. |

---

## Daily Process (07:00 AM Cron Job)

1. Locate new `incoming_transaction` entries that have not yet been staged.
2. Mark these transactions as staged and copy them into `staged_transaction`.
3. Group the staged transactions and create/update a `global_transaction_ticket` containing:
    - Organization reference
    - Total accumulated amount

The ticket remains **ack=false** until it is acknowledged.

---

## Sending Tickets to the Global Server

The system sends @7am -> any **ack=false** `global_transaction_ticket` to the Global Server via a secure API.

Each ticket includes:
- The ticket key
- Organization reference
- Accumulated total amount
- ticket reference
This enables the Global Server to process payout to the organization.

---

## Receiving Settlement Acknowledgment

Once the Global Server completes payout, it responds with:
- The ticket key
- The confirmed paid amount
- A verification signature

### Upon matching and verification:
- The ticket is marked **ack=false**
- Linked `staged_transaction` records are moved into `ack_transaction`
- The related `incoming_transaction` records are marked **ack=true**
- The entries are removed from `staged_transaction`

This completes the reconciliation for those transactions.

---


