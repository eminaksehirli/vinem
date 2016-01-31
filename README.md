Visual Interactive Neighborhood Miner (VINeM)
---------------------------------------------

This software package is the implementation of the software which is introduced
in "Visual Interactive Neighborhood Mining on High Dimensional Data" by
[Emin Aksehirli][], [Bart Goethals][], and [Emmannuel Müller][Mueller] in KDD 2015
Workshop on Interactive Data Exploration and Analytics - [IDEA 2015][idea].
Please check the [web site][] for more information and the paper.

[Emin Aksehirli]:http://memin.tk/
[Bart Goethals]:https://www.uantwerpen.be/en/staff/bart-goethals/
[Mueller]:https://hpi.de/mueller/start.html
[idea]:http://poloclub.gatech.edu/idea2015/
[web site]:http://adrem.uantwerpen.be/vinem

## Building the package

An already-built jar file of the porject is provided on the [web site][].

This application is coded in Java and uses [Apache Maven][] for dependency
management and build system. After properly installing Java and Maven give the
following command in this folder to create an executable jar file:

    mvn clean compile assembly:single

An executable `.jar` file will be crated in the `target` directory. The jar file
is self-contained, that is it includes all the dependencies. Its name is
`vinem-VERSION-jar-with-dependencies.jar`. Note that an Internet connection is
needed during the first run for the download of depencies.

## Contact

For more information please contact [Emin Aksehirli][]. Or visit the project
repository on https://gitlab.com/cartification/vinem.

