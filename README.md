KiwiPredator
============

Copyright 2013 Zekai Huang

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   



<img src="http://www.brainmaker.org/wp-content/uploads/2013/02/sometimes_evolution_sucks_poster-r7d659c1b8dc24d679a5f70f44af7277d_w2u_400.jpg" alt="Sometimes, evolution sucks!">


Sometimes, evolution sucks! That's why we are building Artificial Intelligence.

KiwiPredator is a first orderized wikipedia, which extracts first order predicate calculus from Wikipedia and stores in <a href="https://github.com/Zekai/EulerDB">EulerDB<a>. 

NOTE: This project is currently nothing more than some scratching code, thus not really following any oop, coding practise. 

============

1. Architecture

<img src="http://www.brainmaker.org/wp-content/uploads/2013/07/architecture.png" alt="Architeture">

2. Components

2.1 Parser

Parser is the component that converts sentence into a parser tree. Which is done by Stanford Parser. 

2.2 Extractor

Extractor is the component that extract subject, predict and object from a sentence. It contains two main parts -- a markup language called tree-based regular expression language (TREL), its interpreter and a tregex engine which perform regular expression on a parser tree.

TREL has two characteristics.

* Markup Language
* Inheritance

There are three main types of ERML. Splitting, Pruning and Replacing.

* Splitting: Mainly used for splitting one tree into several subtrees.
* Pruning: Mainly used for pruning uninterested parts.
* Replacing: Mainly used for replacing pronoun, tense and number type.

2.3 Evaluator

Evaluator reviews the hypothesis and either takes it as a new item of knowledge or discard it. In either case, it will send feedback to the TREL. 
