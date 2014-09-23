[![Stories in Ready](https://badge.waffle.io/lvh/icecap.png?label=ready&title=Ready)](https://waffle.io/lvh/icecap)
# icecap

[![Build Status](https://travis-ci.org/lvh/icecap.svg)](https://travis-ci.org/lvh/icecap)

**This product makes security claims that have not been backed up by
  impartial third party audit. Use it at your own peril!**

`icecap` is a capability system based on URLs.

## What are capabilities?

A capability ("cap") gives you the authority to perform some action,
without giving you any *other* authority. In `icecap`, caps are
represented by URLs.

## Why would I want capabilities?

Capabilities make it easy to build systems that follow the principle
of least authority. Simply put: a component (such as a piece of
software) should only have the rights to do what it *needs* to do.
That sounds obvious, but this principle is rarely applied in practice.
Applications are often trusted with credentials with capabilities far
beyond what the application actually needs. This makes those
applications an interesting target for attacks.

For example, applications are often given API keys that provide access
to an entire service, when in reality they only use one particular
feature in a very specific way. If your application writes some output
to cloud storage, there is no reason why it should have credentials
that allow it to spin up servers.

Typically, those credentials are also provided to the application in
an insecure fashion. Secure configuration and secret management is not
a problem `icecap` aims to solve: capability URLs are still privileged
credentials, just less so. For secure secrets management, see
[Barbican](https://wiki.openstack.org/wiki/Barbican).

These are capabilities in the object-capability sense, a programming
paradigm based on this notion of limited capabilities to increase the
security of computer programs. For more information about object
capability systems, take a look at [E][E], a programming language
built around them. For people who have heard of capability systems
before, but have not yet been convinced of their merits, consider the
paper ["Capability Myths Demolished"][CapMyths], by the same authors.

[E]: http://erights.org/
[CapMyths]: http://www.erights.org/elib/capability/duals/myths.html

## What does it *do*?

When you submit a plan of what you would like the cap to do, it
returns a URL, which can then be used to exercise or revoke the cap.

You're then free to pass that URL around to whoever needs its
functionality, without having to give them the credentials that the
capability actually uses internally.

Once the cap is revoked, future attempts to exercise it fail.

Caps are immutable. They are only ever in one of two states: they
either exist, or they don't.

## Differences with other capability systems

(This section is mostly of interest for people who have used other
capability systems.)

Most other capability systems are entire programming environments. By
contrast, icecap is a service. That has upsides and downsides.

Good:

- icecap is trivial to use from programming languages, putting the
  tools to work by the principle of least authority in everyone's
  hands.
- icecap provides cryptographic guarantees. Alternative systems
  typically provide guarantees from language or sandbox semantics. A
  compromised service on the same host can break a classic capability
  system; icecap maintains all its guarantees even when all servers
  are compromised.
- icecap can run as a service, taking operational complexity away from
  the user.

Bad:

- In icecap, capabilities are encrypted and written to durable
  storage. By contrast, most capability systems are in-memory. This
  means that exercising and creating icecap capabilities is much, much
  slower than classic in-memory capabilities; several milliseconds
  versus several CPU cycles. As a result, icecap is only applicable in
  circumstances where that is acceptable, such as network processes.
- Despite the cryptographic guarantees, if icecap is being hosted by a
  third-party hosting provider, you have to trust that they're
  actually hosting icecap, and not icecap with a back door.

To recap, icecap gives you a trade-off. It is an application of the
same ideas that make classic capability systems great. It is not in
competition with those systems, because it fills a specific niche of
coordinating processes over a network.

In icecap, revocation is a first-class feature. This is slightly
different from some other capability systems, where revocation is
typically achieved by composition with an intermediary revoking
object. In icecap, you can't mutate that intermediary object, because
it's immutable. This doesn't turn out to make much of a difference: in
both systems you compose capabilities in order to achieve confinement;
the only difference is if you revoke a capability directly (icecap) or
indirectly (other capability systems).

Because capabilities are URLs, they can be freely shared. This is a
material difference from most object-capability systems, where
capabilities themselves can only be shared through invocations, which
only travel over existing capabilities. For example, consider the
following classic Granovetter diagram, from
["Capability Myths Demolished"][CapMyths]:

![Gramovetter diagram](http://www.erights.org/elib/capability/duals/images/granovetter.gif)

In order for Alice to share her reference to Carol with Bob through
the "foo" message (the arrow), Alice must of course first have a
reference to Bob in the first place. Within the confines of an object
capability system, the only way that can happen is through a
capability. In icecap, by contrast, capabilities are typically
communicated through some unspecified side channel, such as a
configuration file.

This means that icecap does not (and can not) solve the confinement
problem: if you give someone a capability, you can't limit their
ability to share that capability. (Technically, you can't do that in
an object capability system either: instead, you limit the people that
someone you give a capability to can communicate with.)

## License

Copyright © 2014 the icecap authors (see AUTHORS)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
