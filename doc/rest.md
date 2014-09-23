# REST API v0

The only API version right now is `v0`.

API versions work like SemVer major version numbers: absolutely
anything and everything can change without warning in `v0`. Once the
API is stable, it will be called `v1`. Future backwards incompatible
changes cause version bumps.

The only real API is:

- `POST /v0/capabilities` creates a new cap.

Additionally, there are APIs for exercising and revoking caps:

- `GET /v0/capabilities/{cap}` exercises a cap.
- `DELETE /v0/capabilities/{cap}` revokes a cap.

However, you should use the URLs returned when creating capabilities,
rather than producing these URLs manually.

## Creating capabilities

To create a cap, POST some [EDN][EDN] describing it.

[EDN]: https://github.com/edn-format/edn

Currently, this description only consists of a *plan*, which describes
the actions to be taken when the cap is exercised.

A plan is one of these things:

1. A map, describing a single step.
2. An vector of one or more plans.
3. An set of one or more plans.

This means that plans can be nested, and that eventually it's single
steps (maps) all the way down.

A vector, being an *ordered* collection, implies *order* in the plans.
Any preceding plans must be completed successfully before a plan can
be attempted.

A set, being an *unordered* collection, implies no order in the plans.
They may all be attempted with any amount of concurrency and in any
order, as the implementation sees fit.

### Step maps

The exact schema of a step map depends on the kind of step being
defined.

TODO: find a decent way to link to the actual schemas in the code

### Examples

TODO: write examples

## Deleting a cap

TODO: explain how deleting a cap works
