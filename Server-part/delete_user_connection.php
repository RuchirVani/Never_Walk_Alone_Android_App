<?php

     require_once('dbconnect.php');

     db_connect();

     $Sender = $_GET["Sender"];
     $Observer = $_GET["Observer"];
     
     $sql="delete from connected_users where User='".$Sender."' AND Observer='".$Observer."'";

          echo $sql;

     $result = mysql_query($sql);
     if(!$result)
     {
        throw new Exception('Query failed: ' . mysql_error());
        exit();
     }

?>





