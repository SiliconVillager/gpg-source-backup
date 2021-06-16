for i in *.xml ; do xsltproc -v -o $i.html ../../../../doc/documentation/xml2ifdothen.xslt $i;  done

{
cat << INTRO
<?xml version="1.0" encoding="UTF-8"?>
<html xmlns="http://www.w3.org/TR/xhtml1/strict">
  <head>
    <title>if do than rules files</title>
  </head>
  <body>
INTRO


for i in *.xml ; do echo "<a href=$i.html> $i </a><br>";  done

cat << OUTRO
  </body>
</html>
OUTRO
} > processSpec.html
