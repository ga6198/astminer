[![JetBrains Research](https://jb.gg/badges/research.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![CircleCI](https://circleci.com/gh/JetBrains-Research/astminer.svg?style=svg)](https://circleci.com/gh/JetBrains-Research/astminer)
[ ![Download](https://api.bintray.com/packages/egor-bogomolov/astminer/astminer/images/download.svg) ](https://bintray.com/egor-bogomolov/astminer/astminer/_latestVersion)

#The php extractor is run from astminer/src/main/kotlin/astminer/Main.kt

# astminer
A library for mining of [path-based representations of code](https://arxiv.org/pdf/1803.09544.pdf) and more, supported by the [Machine Learning Methods for Software Engineering](https://research.jetbrains.org/groups/ml_methods) group at [JetBrains Research](https://research.jetbrains.org).

Supported languages of the input:

- [x] Java
- [x] Python
- [x] C/C++
- [x] Javascript (beta) (see [issue](https://github.com/vovak/astminer/issues/22))

### Version history

#### 0.5

* Beta of Javascript support
* Storage of ASTs in [DOT format](https://www.graphviz.org/doc/info/lang.html)
* Minor fixes

#### 0.4

* Support of code2vec output format
* Extraction of ASTs and path-based representations of individual methods
* Extraction of data for the task of method name prediction ([code2vec paper](https://arxiv.org/abs/1803.09473))

#### 0.3

* Support of C/C++ via [FuzzyC2CPG parser](https://github.com/ShiftLeftSecurity/fuzzyc2cpg)

#### 0.2

* Mining of ASTs

#### 0.1
* astminer is available via Maven Central
* Support of Java and Python
* Mining of [path-based representations of code](https://arxiv.org/pdf/1803.09544.pdf)


## About
Astminer is an offspring of an internal utility from our ongoing research project.

Currently it supports extraction of:
* Path-based representations of files
* Path-based representations of methods
* Raw ASTs

Supported languages are Java, Python, C/C++, but it is designed to be very easily extensible.

For the output format, see the section below.

## Usage

### Use as CLI

See a [subfolder](/astminer-cli) containing CLI and its description. It can be extended if needed.

### Integrate in your mining pipeline

#### Import

Astminer is available in [Bintray repo](https://bintray.com/egor-bogomolov/astminer/astminer). You can add the dependency in your `build.gradle` file:
```
repositories {
    maven {
        url  "https://dl.bintray.com/egor-bogomolov/astminer" 
    }
}

dependencies {
    compile 'io.github.vovak.astminer:astminer:0.5'
}
```

If you use `build.gradle.kts`:
```
repositories {
    maven(url = "https://dl.bintray.com/egor-bogomolov/astminer/")
}

dependencies {
    compile("io.github.vovak.astminer", "astminer", "0.5")
}
```

#### Examples

If you want to use astminer as a library in your Java/Kotlin based data mining tool, check the following examples:

* A few [simple usage examples](src/main/kotlin/astminer/examples) can be run with `./gradlew run`.

* A somewhat more verbose [example of usage in Java](src/main/kotlin/astminer/examples/AllJavaFiles.kt) is available as well. 

Please consider trying Kotlin for your data mining pipelines: from our experience, it is much better suited for data collection and transformation instruments.

### Output format

For path-based representations, astminer supports two output formats. In both of them, we store 4 `.csv` files:
1. `node_types.csv` contains numeric ids and corresponding node types with directions (up/down, as described in [paper](https://arxiv.org/pdf/1803.09544.pdf));
2. `tokens.csv` contains numeric ids and corresponding tokens;
3. `paths.csv` contains numeric ids and AST paths in form of space-separated sequences of node type ids;
4. `path_contexts.csv` contains labels and sequences of path contexts (triples of two tokens and a path between them).

If the replica of [code2vec](https://github.com/tech-srl/code2vec) format is used, each line in `path_contexts.csv` starts with a label, 
then it contains a sequence of space-separated triples. Each triple contains start token id, path id, end token id, separated with commas.

If csv format is used, each line in `path_contexts.csv` contains label, then comma, then a sequence of `;`-separated triples.
Each triple contains start token id, path id, end token id, separated with spaces.

## Other languages

Support for a new programming language can be implemented in a few simple steps.

If there is an ANTLR grammar for the language:
1. Add the corresponding [ANTLR4 grammar file](https://github.com/antlr/grammars-v4) to the `antlr` directory;
2. Run the `generateGrammarSource` Gradle task to generate the parser;
3. Implement a small wrapper around the generated parser.
See [JavaParser](src/main/kotlin/astminer/parse/antlr/java/JavaParser.kt) or [PythonParser](src/main/kotlin/astminer/parse/antlr/python/PythonParser.kt) for an example of a wrapper.

If the language has a parsing tool that is available as Java library:
1. Add the library as a dependency in [build.gradle.kts](/build.gradle.kts);
2. Implement a wrapper for the parsing tool.
See [FuzzyCppParser](src/main/kotlin/astminer/parse/cpp/FuzzyCppParser.kt) for an example of a wrapper.

## Contribution
We believe that astminer could find use beyond our own mining tasks.

Please help make astminer easier to use by sharing your use cases. Pull requests are welcome as well.
Support for other languages and documentation are the key areas of improvement.

## Citing astminer
A [paper](https://zenodo.org/record/2595271) dedicated to astminer (more precisely, to its older version [PathMiner](https://github.com/vovak/astminer/tree/pathminer)) was presented at [MSR'19](https://2019.msrconf.org/). 
If you use astminer in your academic work, please consider citing it.
```
@inproceedings{kovalenko2019pathminer,
  title={PathMiner: a library for mining of path-based representations of code},
  author={Kovalenko, Vladimir and Bogomolov, Egor and Bryksin, Timofey and Bacchelli, Alberto},
  booktitle={Proceedings of the 16th International Conference on Mining Software Repositories},
  pages={13--17},
  year={2019},
  organization={IEEE Press}
}
```
