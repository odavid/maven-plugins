File indexFile = new File( basedir, "target/site/foo-report/preconfigured-report.html" );
File subDirFile = new File( basedir, "target/site/foo-report/ooo/test-subdir.txt" );

assert indexFile.isFile()
assert subDirFile.isFile()
