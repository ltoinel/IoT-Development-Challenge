# ![Gatling](http://gatling.io/images/gatling-logo.png)

## Synopsis

Ce script en scala/DSL inject 100 000 valeurs aléatoires vers un micro service par le biais de 10 injecteurs, de façon concurente  

##Download

[Gatling Download](http://gatling.io/#/download)

##Installation

[Quickstart](http://gatling.io/docs/2.0.0-RC2/quickstart.html)

le script a besoin de certaines librairies pour pouvoir envoyer les résultats des tests:

-Téléchargez les fichiers jar de [HttpComponents,HttpClient 4.5.2](https://hc.apache.org/downloads.cgi)

-Ajoutez les au dossier lib de votre gatling

-Déposer le fichier gatling_script.scala dans \user-files\simulations

##Mode d'emploi

- Vous avez qu'à lancer le gatling.bat sur Windows ou gatling.sh sur Linux et lancer la class 'Injections' en tapant son numéro.
- Si vous voulez lancer le script de votr shell sur Windows :gatling.sh -s Injections 
#### N'oublier d'ajouter le nom de votre equipe (TeamName)et de votre rattachement(TeamLocation) sur le script avant de le compiler/executer

[Documentation](http://gatling.io/docs/2.0.0-RC2/index.html)

##Team

[Ludovic Toinel](https://github.com/ltoinel)

[Benabachir Issam](https://github.com/IsBena)

[Pavageau Stanislas](https://github.com/StanislasCapgemini )



