File file = new File(basedir, "target/sym/log4j/META-INF")
println file.getAbsolutePath()
assert file.exists() && file.isDirectory();