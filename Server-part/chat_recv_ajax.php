<?php

     require_once('dbconnect.php');

     db_connect();
	 
     $Username = $_GET["Username"];
	 $sql = "SELECT * from location_update where Username='".$Username."' order by ID desc limit 1";
     //$sql = "SELECT * FROM (" . $sql . ") as ch order by ID";
     $result = mysql_query($sql) or die('Query failed: ' . mysql_error());
     
     // Update Row Information
     
     /*while ($line = mysql_fetch_array($result, MYSQL_ASSOC))
     {
           $msg = $msg . "<tr><td>" . $line["cdt"] . "&nbsp;</td>" .
                "<td>" . $line["username"] . ":&nbsp;</td>" .
                "<td>" . $line["msg"] . "</td></tr>";
     }
     $msg=$msg . "</table>";
     */
	 while ($line = mysql_fetch_array($result, MYSQL_ASSOC))
     {		
		  $msg = $line["Latitude"]." ".$line["Longitude"]." ".$line["Panic"];
                 
                 
     }
     echo $msg;

?>





