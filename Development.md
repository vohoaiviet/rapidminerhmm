#How to build and extend the RapidMiner HMM plugin.

# How to build the project from sources #

  1. checkout RapidMiner (RM) source code via SVN from
https://rapidminer.svn.sourceforge.net/svnroot/rapidminer
  1. checkout source code of this project
  1. change rm.dir property in `trunk/HMM/Vega/build.xml` file so it points to the dir with latest RM sources, in my case it is `<property name="rm.dir" location="../../../Vega" />`
  1. build the plugin by executing `ant` command in `trunk/HMM/Vega/` directory