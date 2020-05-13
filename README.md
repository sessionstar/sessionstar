# Session\*: A Toolchain for Statically-Verified Refinements for Multiparty Protocols

This repository hosts the source code for Session\*, a toolchain for
implementing multiparty protocols with refinements.

This is an anonymised pre-artifact accompanying the OOPSLA paper submission.
A final artifact, containing source codes, benchmarks, and examples, will be
submitted for evaluation upon paper acceptance.

## How to use

The easiest way to using the toolchain is via a Docker image.

You can obtain the image via

```bash
docker pull sessionstar2020/sessionstar
```

and start a container with the image with

```bash
docker run --rm -it sessionstar2020/sessionstar
```

## Contents

`scribble` directory contains an extended version of Scribble, which provides
the functionality to specify multiparty protocols with refinements.
The extended Scribble validates a protocol, and projects it to participating
roles in a form of a Communicating Finite State Machine (CFSM) for code
generation.

`ScribbleCodeGenOCaml` directory contains a code generator, which takes the CFSM
output from Scribble to generate F\* code.

`sessionstar` is a convenient script to run both stages of the toolchain.
You can use it via

```bash
sessionstar FILE_TO_SCRIBBLE_PROTOCOL PROTOCOL ROLE
```

e.g.

```bash
sessionstar HigherLower.scr HigherLower B
```

## Contact

The authors can be reached at sessionstar2020#gmail.com (replace # with @)
