# FreeCC Parser Generator 0.9.4 preview 

I (revusky) decided to dust off this old unfinished open source project, abandoned since early 2009. 

There were four publicly released versions of FreeCC back when, on Google Code, which is now defunct. There is an archived version of the site [here](https://code.google.com/archive/p/freecc/).

The last version released in January 2009 was labeled 0.9.3. Therefore I have taken up that naming, and the current release is 0.9.4. Version numbers are quite arbitrary, of course. Thse version numbers are somewhat misleading as regards the maturity of the tool. FreeCC is based on forking the JavaCC codebase in April of 2008. That version of JavaCC was 4.1, as I recall, and this 0.9.x version of FreeCC is a (vastly) more advanced version of that JavaCC tool. I noticed that the JavaCC people put out more versions since then, labeling it JavaCC 6.0. But a quick comparison of that with the version available in 2008 shows that there is virtually no diffeerence.

The original intention was not to fork a new project, but to contribute all of these enhancements to JavaCC. However, in their infinite wisdom, the maintainers of the project declined the contribution. Actually, they even declined to look at it!

## Changes in the new resuscitated codebase

As of this writing, I have added tweaked the grammar to support the additions to the Java language up until Java 7. (I think I have, though it requires more formal testing.) I also fixed some various bugs that were there relating to much older additions to the language, specifically problems with properly parsing Generics syntax and also there was as bug with Enums.

The main new thing that FreeCC does not (yet) support is Lambda expressions. I intend to remedy this fairly soon.

FreeCC is not very well documented at the moment. There is some rudimentary information on the [archived GoogleCode site](https://code.google.com/archive/p/freecc/wikis)

It should be possible to check the code out and just do an ant build from the top directory, by just running ant. You should also be able to run a little test suite by running "ant test".

If you are interested in this project, either as a user or as a developer, please do [write me](a href="mailto:revuskyNOSPAM@gmail.com").
