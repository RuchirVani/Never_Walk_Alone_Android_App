<?php

     require_once('dbconnect.php');

     db_connect();

     $Latitude = $_GET["Latitude"];
     $Longitude = $_GET["Longitude"];
     $Username = $_GET["Username"];
	 $Panic= $_GET["Panic"];

     $sql="INSERT INTO location_update(Latitude,Longitude,Username,Panic) " .
          "values(" . quote($Latitude) . "," . quote($Longitude) . "," . quote($Username) . "," . quote($Panic).");";

          echo $sql;

     $result = mysql_query($sql);
     if(!$result)
     {
        throw new Exception('Query failed: ' . mysql_error());
        exit();
     }

?>





