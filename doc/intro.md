# Introduction to icecap

`icecap` is a system for generating capability URLs.

Capability URLs are unguessable URLs that cause some action to be
performed when they are accessed. Having that reference (the URL)
gives you the authority to perform some action, but does not give you
any *other* authority.

This makes it easy to build computer systems that follow the principle
of least authority. Simply put, that means that a component should
only have the rights to do what it *needs* to do. That sounds obvious,
but it is rarely applied in practice. Applications are often trusted
with credentials, such as API keys, with capabilities far beyond what
the application actually needs. This makes those applications an
interesting target for attacks.

Typically, those credentials are also provided to the application in
an insecure fashion. Secure configuration and secret management is not
a problem that icecap aims to solve: capability URLs are still
privileged credentials, just less so. For secure secrets management,
see [Barbican](https://wiki.openstack.org/wiki/Barbican).

These are capabilities in the object-capability sense, a programming
paradigm based on this notion of limited capabilities to increase the
security of computer programs. For more information about
object-capability systems, take a look at [E][E], a programming
language built around them. For people who have heard of capability
systems before, but have not yet been convinced of their merits,
consider the paper ["Capability Myths Demolished"][CapMyths], by the
same authors.

[E]: http://erights.org/
[CapMyths]: http://www.erights.org/elib/capability/duals/myths.html

## What does it do?

It lets create a capability, which means that you submit a description
of what the capability does, called a plan. It returns a capability
URL, which can be used to exercise or revoke the capability.

When the capability is exercised, the plan is executed.

When the capability is revoked, future attempts to exercise it fail.

Capabilities and their URLs are immutable. They are only ever in one
of two states; extant or non-extant. This has the interesting side
effect that whoever has a capability URL that's being used more than
once, knows that it's going to do (more or less) the same thing that
it did last time.

# REST API v0

The only API version right now is `v0`.

API versions work like SemVer major version numbers: absolutely
anything and everything can change without warning in `v0`. Once the
API is stable, it will be called `v1`. Future backwards incompatible
changes cause version bumps.

The following paths are supported:

- `POST /v0/capabilities` creates a new capability.
- `GET /v0/capabilities/{capability_id}` exercises a capability.
- `DELETE /v0/capabilities/{capability_id}` revokes a capability.

## Creating capabilities

To create a capability, POST some [EDN][EDN] describing it.

[EDN]: https://github.com/edn-format/edn

Currently, this description only consists of a *plan*, which describes
the actions to be taken when the capability is exercised.

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

## Deleting a capability

TODO: explain how deleting a capability works

# Security considerations

`icecap` only stores encrypted blobs. In order to decrypt a blob, you
need both the matching capability URL (which is never stored within
icecap) and an icecap installation-wide secret (stored on the API
servers). Furthermore, the blobs are stored at indices which can also
only be computed from the capability URL and the installation-wide
secret.

That means that even in the case of a complete compromise of database
and API servers, no user data is compromised. Without the capability
URL, an attacker can not compute the index into the database nor the
encryption key used to encrypt the blob at that index.

An attacker can not deterministically modify a blob, even if they
don't care *which* blob, because all blobs are securely authenticated
and encrypted.

## How does it actually work?

This section describes the meat and potatoes of icecap's cryptographic
backing.

### Definitions

- The *capability identifier* is the unique identifier of the
  capability. It is randomly selected when the capability is created.
  It is returned as part of the capability URL, but is never stored by
  icecap.
- The *master key* is a secret key stored on all of the API endpoints.
  It is used in combination with a capability identifier to produce
  the index and capability key.
- The *index* is the location in the database where the blob is
  stored. It is produced from the capability identifier and the master
  key.
- The *blob* the ciphertext of the plan. The database stores it at the
  index.
- The *capability key* is the key used to encrypt the plan. It is
  produced from the capability identifier and the master key.
- The *plan* is a description of what the capability actually does
  when it is exercised.
- The *salt* is a not-necessarily-secret (although typically kept
  secret) parameter to the key derivation function.

### Constants

- Capability identifier size (bits): 256
- Master key size size (bits): 256
- Index size size (bits): 256
- Capability key size (bits): 256
- Salt size (bits): 256

### Assumptions

Random secret keys are produced by a cryptographically secure
pseudorandom number generator. Virtually always, this should be
`/dev/urandom`, `CryptGenRandom` or `java.security.SecureRandom`
(provided that it boils down to one of the previous two).

### Key derivation

Because of its security and performance, BLAKE2 is used in single-pass
mode as a key derivation function. Specifically, this means:

```
k = blake2b(digest_size=CAPABILITY_KEY_SIZE + INDEX_SIZE,
            salt=SALT,
            key=MASTER_KEY,
            person=CAPABILITY_IDENTIFIER).digest()
enc_key, index = k[:CAPABILITY_KEY_SIZE], k[-INDEX_SIZE:]
```

Since this document is still a draft, this is subject to change. See
[lvh's blog post][blog-kdf] for more information about relative
performance of key derivation functions.

While the salt is assumed to be secret, this is not a necessary
condition for secure key derivation. For more information on how salts
(any salt, but a fortiori a secret one) can impact security, consult
the [HKDF paper][hkdf-paper].

[blog-kdf]: http://www.lvh.io/posts/secure-key-derivation-performance.html
[hkdf-paper]: http://eprint.iacr.org/2010/264

### Authenticated encryption

Authenticated encryption is done using the NaCl `secretbox` scheme,
which is a provably secure encrypt-then-authenticate MAC composition
scheme using state-of-the-art cryptographic primitives. For more
details about provably {IND,NM}-CCA2 secure MAC composition, see
[Krawczyk01][Krawczyk01] and [Bellare07][Bellare07]. For more details
about the `secretbox` scheme in NaCl, take a look at
[its website](http://nacl.cr.yp.to/secretbox.html). Finally, for more
details about the components, consider their respective websites:
[XSalsa20](http://cr.yp.to/snuffle.html) and
[Poly1305](http://cr.yp.to/mac.html)).

[Krawczyk01]: http://www.iacr.org/archive/crypto2001/21390309.pdf
[Bellare07]: http://cseweb.ucsd.edu/~mihir/papers/oem.pdf

### Set up

An `icecap` installation picks a (secret) master key and a (ideally
secret, although this isn't a hard requirement) salt. The master key
and salt are shared amongst all API endpoints.

### Creating a capability identifier

First, randomly select a capability identifier of the appropriate
size.

Derive key material from the master using the key derivation function,
producing the index and the capability key.

The identifier is encoded in base64url (base64 with a URL-safe
alphabet), as described by [RFC 4648, section 5][RFC4648]. Since the
length is known ahead of time, padding is removed, as suggested by the
RFC.

[RFC4648]: http://www.ietf.org/rfc/rfc4648.txt

After the URL has been handed to the creator of the capability, it is
discarded.

### Exercising a capability

When the URL is called, the (base64url-encoded) capability identifier
is extracted and decoded. As when creating the capability, the
identifier is used to produce the index and capability key. The
capability is looked up, decrypted and executed.

# Reliability considerations

In order to maximize flexibility and reliability, icecap actively
avoids demanding a lot from its backend: it only desires simple
key-value storage, where both are opaque binary blobs.

## Read consistencies in distributed databases

Because the values are immutable, and either exist or do not exist, it
is possible to use high-availability distributed database while
maintaining very fast, 1-machine read consistency levels.

However, this raises an issue for deleting capabilities. In order to
make sure that a successful response to deleting a capability can
never be followed by successfully exercising the capability, one of
the following things must happen:

 - Both reads and deletes happen with a quorum majority. This is
   undesirable because it makes reads significantly slower, which is
   the common operation.
 - Deletes happen with *cluster-wide* consistency. This is undesirable
   because it means that deletes will occasionally fail.
 - Middle ground: deletes happen with quorum, and the user can
   optionally ask for "strong", cluster-wide deletes. (There is no
   point in adding a similar feature for reads, when working on the
   assumption that the caller would always rather exercise the
   capability than discover that it has been revoked.)
 - Middle ground #2: for a cluster of *N*, reads happen with low
   consistency *R*, deletes happen with consistency *N - R + 1*. The
   reads don't use "consistency" to determine that state of the data
   (since it's immutable), but merely to assert if it still exists.
   This means both reads and deletes will always be consistent, and
   allows for configurable trade-off between read and delete
   performance.

Presumably the last one is the most desirable one, but this can be
revisited at a later date when a non-distributed prototype has been
produced and perhaps the problem is better understood.
