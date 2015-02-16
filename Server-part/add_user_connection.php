<?php

     require_once('dbconnect.php');

     db_connect();

     $Sender = $_GET["Sender"];
     $Observer = $_GET["Observer"];
     
     $sql="INSERT INTO connected_users(User,Observer) " .
          "values(" . quote($Sender) . "," . quote($Observer).");";

          echo $sql;

     $result = mysql_query($sql);
     if(!$result)
     {
        throw new Exception('Query failed: ' . mysql_error());
        exit();
     }

?>





