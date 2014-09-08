# icecap

[![Build Status](https://travis-ci.org/lvh/icecap.svg)](https://travis-ci.org/lvh/icecap)

**This product makes security claims that have not been backed up by
  impartial third party audit. Use it at your own peril!**

`icecap` is a capability system based on URLs.

## What are capabilities, and why do I want them?

A capability ("cap") gives you the authority to perform some action,
without giving you any *other* authority. In `icecap`, caps are
represented by URLs.

This makes it easy to build systems that follow the principle of least
authority. Simply put: a component (such as a piece of software)
should only have the rights to do what it *needs* to do. That sounds
obvious, but is rarely applied in practice. Applications are often
trusted with credentials with capabilities far beyond what the
application actually needs. This makes those applications an
interesting target for attacks.

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

This is slightly different from some other capability systems you may
have used, where revocation is typically achieved by composition with
an intermediary revoking object. Because icecap embraces immutable
capabilities, you can't mutate that intermediary object. As a result,
revocation has to be a first class feature. This turns out to be an
uninteresting difference in practice: in both systems you compose
capabilities in order to achieve confinement; the only difference is
if you revoke a capability directly (icecap) or indirectly (other
capability systems).

## License

Copyright © 2014 the icecap authors (see AUTHORS)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
