# Security considerations

`icecap` only stores encrypted blobs. In order to decrypt a blob, you
need both the cap (which is never stored within icecap) and an icecap
installation-wide secret (stored on the API servers). Furthermore, the
blobs are stored at indices which can also only be computed from the
cap and the installation-wide secret.

That means that even in the case of a complete compromise of database
and API servers, no user data is compromised. Without the cap, an
attacker can not compute the index into the database nor the
encryption key used to encrypt the blob at that index.

An attacker can not deterministically modify a blob, even if they
don't care *which* blob, because all blobs are securely authenticated
and encrypted.

## How does it actually work?

This section describes the meat and potatoes of icecap's cryptographic
backing.

### Definitions

- The *cap* is a unique identifier, randomly selected when the cap is
  created. It is never stored by icecap.
- The *seed key* is a secret key stored on all of the API endpoints.
  It combined with the cap to produce the index and cap key.
- The *index* is the location in the database where the blob is
  stored. It is produced from the cap and the seed key.
- The *blob* the ciphertext of the plan. The database stores it at the
  index.
- The *cap key* is the key used to encrypt the plan. It is produced
  from the cap and the seed key.
- The *plan* is a description of what the cap actually does when it is
  exercised.
- The *salt* is a not-necessarily-secret (although typically kept
  secret) parameter to the key derivation function.

### Constants

- Cap size (bits): 256

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
            key=SEED_KEY,
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

An `icecap` installation picks a (secret) seed key and a (ideally
secret, although this isn't a hard requirement) salt. The seed key and
salt are shared amongst all API endpoints.

### Creating a cap

First, randomly select a cap of the appropriate size. (Caps are
sufficiently long that randomly choosing one results in a negligible
probability of collision.)

From the cap, produce the index and the cap key using the key
derivation function.

The cap is encoded in base64url (base64 with a URL-safe alphabet), as
described by [RFC 4648, section 5][RFC4648]. Since the length is known
ahead of time, padding is removed, as suggested by the RFC.

[RFC4648]: http://www.ietf.org/rfc/rfc4648.txt

After the URL has been returned, it is discarded.

### Exercising a capability

When the URL is called, the (base64url-encoded) capability identifier
is extracted and decoded. As when creating the capability, the
identifier is used to produce the index and capability key. The
capability is looked up, decrypted and executed.
