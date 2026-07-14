# Business Model: Rice-Paddy Operations Coordinator

## Classification

- Repository: `cloud-itonami-isic-0112`
- ISIC Rev. 4: `0112`
- Industry: Growing of rice
- Social impact: food-security, rural-employment, environmental-stewardship

## Customer

- Small-to-medium rice-paddy farms (japonica, indica, glutinous, aromatic,
  upland rice)
- Rice cooperatives and contract growers
- Diversified paddy operations that include mixed water-management schedules
- Smallholder rice producers (extension-service integrations)

## Offer

- Paddy field management and record-keeping (acreage, yield, water-level)
- Planting/flooding-drainage/harvest scheduling coordination
- Crop-health and pest/disease/blast-fungus tracking
- Supply procurement coordination
- Audit trail and transparency

## Revenue

- SaaS subscription (per-hectare-per-season pricing)
- Supply chain integration fees
- API access for agronomist/extension-service partners
- Data analytics and reporting add-ons

## Trust Controls

- No direct field/irrigation-equipment operation without human sign-off
- No finalized pesticide-application decisions by the actor
- All field-operation scheduling proposals are proposals, not commands
- Paddy field registration is required before any operation
- All crop-health concerns are automatically escalated
- High-cost supply orders require approval
- Audit ledger is append-only and never editable

## What we do NOT do

- **Agronomic decisions** (what/when/how much to plant, spray, flood, drain,
  harvest) — the farmer/agronomist decides
- **Pesticide-application decisions** — the agronomist/farmer decides
- **Direct field/irrigation-equipment operation** — the robot manages records
  and logistics only
- **Economic decisions** (crop mix, marketing, land use) — remain human
  authority

## Supported Operations

### Field Record Logging
- Planting records (variety, acreage, date)
- Yield records
- Water-level records (flooding depth / drained-paddy state)
- Soil-test data
- Field-condition notes (logging only, not decision-making)

### Field-Operation Scheduling
- Schedule planting, flooding/drainage water-management, harvest windows
- Track equipment/labor/irrigation availability
- Propose follow-up field visits (not order them directly)

### Crop-Health Concern Escalation
- Flag suspected pest infestation
- Report disease symptoms, blast fungus (いもち病), or drought stress
- Automatic escalation to farmer/agronomist

### Supply Procurement
- Seed/seedling orders
- Fertilizer orders
- Equipment procurement (including irrigation pumps)
- Cost threshold escalation for large orders
