<?php

     require_once('dbconnect.php');

     db_connect();
	 
     $Username = $_GET["Username"];
	 $sql = "SELECT * from connected_users where Observer='".$Username."' limit 1";
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
	 //$msg="";
	 while ($line = mysql_fetch_array($result, MYSQL_ASSOC))
     {		
		  $msg =$line["Observer"]." ".$line["User"];
                 
                 
     }
     echo $msg;

?>





