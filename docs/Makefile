DOT_FILES := $(shell find . -type f -name '*.dot')
RENDERED_DOT_FILES = $(patsubst %.dot,%.svg,${DOT_FILES})

BLOCKDIAG_FILES := $(shell find . -type f -name '*.blockdiag')
RENDERED_BLOCKDIAG_FILES = $(patsubst %.blockdiag,%.svg,${BLOCKDIAG_FILES})

ALL_TARGETS := ${RENDERED_DOT_FILES} ${RENDERED_BLOCKDIAG_FILES}

.PHONY: all clean

all: ${ALL_TARGETS}

%.svg: %.dot
	dot -Tsvg $(realpath $<) > $(addprefix ${CURDIR}/,$@)

%.svg: %.blockdiag
	blockdiag -a -T svg $<

clean:
	git clean -fdx
	rm ${ALL_TARGETS}
