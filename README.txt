Contents:

lib/ - third-party libraries
src/ - sources
    msyu/ - utility classes
    main/ - main Waveprint classes
    tools/ - runnable Waveprint classes
        DatabaseTool - sets up database
        MinhashTool - generates permutations for minhash
        WaveprintTool - adds new entries to DB and searches
    resources/
        waveprint.properties - example configuration

All sources are in Java 7.

To build:
$ ant dist

Run any of the tools without arguments to see usage. Examples follow:

To set up database and precompute stuff:
$ java -cp target/dist/waveprint.jar org.shoushitsu.waveprint.DatabaseTool \
    dbsetup db_location src/resources/waveprint.properties
$ java -cp target/dist/waveprint.jar org.shoushitsu.waveprint.MinhashTool \
    perm -d db_location

To add all files under /some/path and /other/path to database:
$ java -cp target/dist/waveprint.jar org.shoushitsu.waveprint.WaveprintTool \
    add db_location /some/path /other/path

To search for all files under /some/path and /other/path:
$ java -cp target/dist/waveprint.jar org.shoushitsu.waveprint.WaveprintTool \
    find db_location /some/path /other/path

Known issues:
- it's slow
- it's unreliable (with the current example config)
- it depends on ffmpeg (this is probably not going away any time soon, if ever)
- it spams unintelligible logs into standard out
