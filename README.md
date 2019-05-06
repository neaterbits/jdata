# salesportal

## License/Lisens

This is a research project without any particular license, which means that standard copyright applies. It is not open source software as it is written for research purposes and thus one does not for now want to feel the responsibility support the software for other use.

Thus it is only here for viewing, and cannot be used for either commercial or non commercial purposes, nor can it be redistributed.

# Oversikt over kataloger

Dette er et FoU prosjekt for å se nærmere på hva som er mulig mtp rask nedlasting oppdatering av grensesnitt via Ajax. Mere informasjon på wiki-sidene.

Kjapp informajon om innhold i noen av katalogene
 - dao - felles grensesnitt til lagring på tjenersiden.
 - index - felles API for indeksering av data på tjenersiden.
 - index-elasticsearch - indeksering i Elasticsearch.
 - index-lucene - indeksering i Lucene for rask lokal testing.
 - dao-jpa - implementasjon for lagring i relasjonsdatabaser, mest for å teste hvordan ville måtte implementeres.
 - dao-xml - implementasjon for lagring av annonsedata som XML-fil.
 - xmlstorage - API som kan kalles fra dao-xml for å lagre XMLer til fil.
 - xmlstorage-filesystem-files - XML storage implementasjon som kaller mot filesystem abstraksjon.
 - filesystem - abstraksjon for filsystemer, f.eks lokalt, S3, zip-fil, Azure Blob Storage
 - filesystem-local - benyttes av XMLStorage til å lagre i lokalt filsystem, for test.
 - filesystem-s3 - benyttes av XMLStorage for lagring av filer i Amazon S3.
 - model - datamodell for annonser, denne består av annoterte javaklasser (@Facet), slik at kan lett legge til nye typer annonser og facets.
 - webpages - websider m/plain Javascript-kode for filtreringsview og bildegalleri.
 - notifications - API for meldings-utsending.
 - notifications-aws - implementasjon for utsending via Amazon SNS.
 - search-common - datamodell for søkekriterier.
 - rest - selve API-tjenestene som kalles fra klientsiden, implementert som JAX-WS (men kalles fra HTTP servlet for tiden siden problemer med å få JAX-WS til å fungere i Jetty).

Dette er en tidlig versjon, men begynner å fungerer noenlunde hele stacken igjennom.

# Forbedringer som bør gjøres, og er under utbedring
 - Javadoc dokumentasjon :)
 - lagre XML, miniatyrbilde og mellomstore bilder som en samlet fil i S3. Slipper da antakelig logikk for synkronisering.
 - cache miniatyrbilder lokalt på EBS, eller i redis eller lignende.
 - cache søkeresultater fra Elasticsearch lokalt i minne på VM i skyen, evt. i redis eller lignende.
 - bytte implementasjon fra varierende størrelse til fast størrelse i bildegalleri, gjør at kan overføre langt mindre data i initiell søkerespons (kun totalt antall søketreff).
 - side for å vise annonsedetaljer. 

Å legge til websider for å legge inn annonser m/innlogging osv. er ute av scope enn så lenge, er søk/filtrering som er mest interessant for egen kompetanseheving.

Har et annet prosjekt for å gjøre snill webscraping mot craigslist for å hente inn noe testdata.
