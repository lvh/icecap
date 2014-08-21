# Introduction to icecap

`icecap` is a system for generating capability URLs.

Capability URLs are unguessable URLs, that, when accessed, cause some
action to be performed. These are capabilities in the
object-capability sense: having the reference (URL) gives you the
authority to perform some action, but does not give you any *other*
authority.

For more information about object-capability systems, take a look at
[E][E], a programming language built around them.

[E]: http://erights.org/

## What does it do?

It lets you submit a description of a pre-canned request. It returns a
capability URL, which can be used to exercise or delete the
capability.

The action performed when a capability is exercised involves making
one or more pre-canned requests. For now, these requests are limited
in two ways:

- HTTP(S) only.
- Fixed; no parametrization.

These restrictions can be revisited once a prototype has been
delivered.

Capabilities and their URLs are immutable: they are only ever in one
of two states; extant or non-extant. This has the interesting side
effect that whoever has a capability URL that's being used more than
once, knows that it's going to do (more or less) the same thing that
it did last time.

# REST API v0

The only API version right now is `v0`.

This is version zero in the SemVer sense: absolutely anything and
everything can change without warning. Once something stable exists,
it'll be replaced with `v1`.

The following paths are supported:

- `POST /v0/capabilities` creates a new capability.
- `GET /v0/capabilities/{capability_id}` exercises a capability.
- `DELETE /v0/capabilities/{capability_id}` deletes a capability.

## Creating capabilities

To create a capability, POST some [EDN][EDN] describing it. This
description is called the *payload*. A payload consists of a map with
the keyword `:requests` as the key, and a request specification as the
value:

```
{:requests REQUEST_SPECIFICATION}
```

[EDN]: https://github.com/edn-format/edn

A request specification can be one of these things:

1. A map, describing a single request.
2. An vector of request specifications.
3. An set of request specifications.

This means that request specifications can be nested.

A vector, being an *ordered* collection, implies *order* in the
request specifications. Any preceding request specifications must be
completed successfully before a request specification can be
attempted.

A set, being an *unordered* collection, implies no order in the
request specifications. They may all be attempted with any amount of
concurrency and in any order, as the implementation sees fit.

### Request maps

A request map contains a keyword `:target`, which maps to a *target*,
which is the URL target of the request. It additionally contains a
number of optional arguments.

```
{:target "https://example.test"}
```

### Examples

TODO: write examples

## Deleting a capability

TODO: explain how deleting a capability works

# Security considerations

`icecap` only stores data in encrypted and authenticated form. Both
the full capability URLs (which are not stored anywhere), and a
deployment-specific secret key are necessary to decrypt the request
payloads.

That means that even in the case of a complete compromise of both
database and API servers, no customer data is compromised. The
database is a key-value store of indices and encrypted payloads, but:

- An attacker has no way of knowing which capability maps to which
  index without having *and* the capability URL *and* the master
  secret *and* the entire database, because both the capability URL
  and the master secret are required to even compute the index.
- An attacker has no way of knowing what the canned request for a
  capability means without having *and* the capability URL *and* the
  master secret *and* the relevant database row, because the
  capability URL and the master secret are required to produce the
  encryption key for the row.
- An attacker has no way of deterministically modifying any encrypted
  payload, *even* if they don't care which payload they
  deterministically modify. This is because the payloads are
  authenticated and encrypted in a provably secure scheme (see below
  for details).

## How does it actually work?

This section describes the meat and potatoes of icecaps' cryptographic
backing.

### Definitions

- The *capability identifier* is the part of the capability that
  uniquely identifies it. It is part of the capability URL. It is
  never stored within icecap.
- The *master key* is a secret key stored on all of the API endpoints.
  It is used in combination with capability identifiers to produce
  indices and capability keys.
- The *index* is the key under which the database stores the encrypted
  payload. It is produced from the capability identifier and the
  master key.
- The *encrypted payload* is the value stored in the database under
  the index. It is the encrypted and authenticated version of the
  payload, under the capability key.
- The *capability key* is the key used to encrypt the capability
  payload. It is produced from the capability identifier and the
  master key.
- The *payload* is a description of what the capability actually does,
  when exercised.
- The *salt* is a not-necessarily-secret (although typically kept
  secret) parameter to the key derivation function, customizing it
  into a family of such things.

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

The identifier is base64 encoded with a URL-safe alphabet. After the
URL has been handed to the creator of the capability, it is discarded.

### Exercising a capability

When the URL is called, the base64-encoded capability identifier is
extracted and decoded. As when creating the capability, the identifier
is used to produce the index and capability key. The capability is
looked up, decrypted and executed.

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

Presumably the last one is the most desirable one, but this can be
revisited at a later date when a non-distributed prototype has been
produced and perhaps the problem is better understood.

As a consequence, I'm recommending that we start with a simple,
centralized and readily available backend, such as SQL or Redis.
