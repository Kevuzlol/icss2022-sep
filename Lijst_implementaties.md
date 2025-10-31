# Bijlage – Voortgang en implementaties ICSS Compiler

## Student
Naam: Kevin Pouwels  
Studentnummer: 2108687  
Klas: ITN-CNI-B  
Datum: 31/10/2025

## Overzicht implementatie-eisen

### 4.1 Algemene eisen
| ID   | Omschrijving                      | Status             |
|------|-----------------------------------|--------------------|
| AL01 | Package-structuur behouden        | ✅                  |
| AL02 | Compileert met Maven + OpenJDK 13 | ✅                  |
| AL03 | Code onderhoudbaar en leesbaar    | ✅                  |
| AL04 | Eigen werk aangetoond             | Assesment vervolgt |

---

### 4.2 Parseren
| ID   | Omschrijving                                  | Status |
|------|-----------------------------------------------|--------|
| PA00 | IHANStack<ASTNode> correct gebruikt           | ✅      |
| PA01 | Eenvoudige opmaak (level0)                    | ✅      |
| PA02 | Variabele declaraties en referenties (level1) | ✅      |
| PA03 | Rekenoperaties + operatorprecedentie (level2) | ✅      |
| PA04 | If/Else constructs (level3)                   | ✅      |
| PA05 | Min. 30 punten behaald uit parser onderdelen  | ✅      |

---

### 4.3 Checker (Semantisch)
| ID   | Omschrijving                                  | Status |
|------|-----------------------------------------------|--------|
| CH00 | Minstens vier checks geïmplementeerd          | ✅      |
| CH01 | Gebruik van gedefinieerde variabelen          | ✅      |
| CH02 | Type-check voor plus/min/keer                 | ✅      |
| CH03 | Geen kleuren in rekenoperaties                | ✅      |
| CH04 | Property-type komt overeen met value-type     | ✅      |
| CH05 | If-condities zijn boolean                     | ✅      |
| CH06 | Variabelen worden enkel binnen scope gebruikt | ✅      |

---

### 4.4 Transformeren (Evaluator)
| ID   | Omschrijving                                | Status |
|------|---------------------------------------------|--------|
| TR01 | Expressies geëvalueerd naar literals        | ✅      |
| TR02 | If/Else geëlimineerd + juiste body behouden | ✅      |

---

### 4.5 Genereren
| ID   | Omschrijving                        | Status |
|------|-------------------------------------|--------|
| GE01 | Geldige CSS2 output                 | ✅      |
| GE02 | Twee-spaties inspringing per niveau | ✅      |

---

### 4.6 Eigen Uitbreiding
| Uitbreiding              | Beschrijving | Max punten | Status            |
|--------------------------|--------------|------------|-------------------|
| Geen uitbreiding gekozen | n.v.t.       | 0/20       | ❌ Niet uitgevoerd |

---

Alle verplichte onderdelen zijn volledig en correct geïmplementeerd.  
Er is geen optionele uitbreiding toegevoegd.