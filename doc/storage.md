# Storage

## Reliability considerations

In order to maximize flexibility and reliability, icecap actively
avoids demanding a lot from its storage back-end: it only desires
simple key-value storage, where both keys and values are opaque binary
blobs.

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
