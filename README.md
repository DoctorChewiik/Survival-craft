# Survival Craft

Mod de survie pour **Minecraft 1.21.1** avec **NeoForge**.

Objectif : rendre la survie plus exigeante et plus immersive, avec la soif, la température, les saisons, les blessures localisées, le bushcraft et des villes post-apocalyptiques en ruine.

## État actuel (phase 1)

- **Soif** : une barre de 10 gouttes au-dessus de la barre de faim.
  - Elle baisse avec le temps, plus vite quand on court, dans les biomes chauds et dans le Nether.
  - À 6 points ou moins, le joueur est ralenti. À 0, il subit des dégâts de déshydratation (mêmes limites que la famine selon la difficulté).
  - En difficulté paisible, elle remonte toute seule.
- **Gourdes** : en courge (3 gorgées), en cuir (5) et en métal (8).
  - Clic droit en visant de l'eau pour remplir (l'eau est **sale**).
  - Clic droit pour boire. L'eau sale peut rendre malade (faim et nausée).
  - La gourde en métal se purifie en l'utilisant sur un **feu de camp allumé**.
- **Boissons vanilla** : lait, pastèque, pommes, baies, soupes et ragoûts hydratent aussi. Une fiole d'eau compte comme de l'eau sale.
- **Commande de test** : `/survivalcraft thirst <0-20>`.
- Tous les réglages sont dans `config/survivalcraft-common.toml` (ou via le bouton Config de la liste des mods).

## Développement

Prérequis : JDK 21 et IntelliJ IDEA (ou un autre IDE Java).

```sh
./gradlew runClient   # lance le jeu avec le mod
./gradlew build       # produit le .jar dans build/libs/
```

Le plan complet du mod est dans le document « Plan du mod Survival craft ».
