-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.0.27-community-nt


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema crobots
--

CREATE DATABASE IF NOT EXISTS crobots;
USE crobots;

DROP TABLE IF EXISTS `crobots`.`4vs4`;
CREATE TABLE  `crobots`.`4vs4` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `robot1` varchar(45) NOT NULL default '',
  `robot2` varchar(45) NOT NULL default '',
  `robot3` varchar(45) NOT NULL default '',
  `robot4` varchar(45) NOT NULL default '',
  `status` integer unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='4vs4 cross table';

DROP TABLE IF EXISTS `crobots`.`3vs3`;
CREATE TABLE  `crobots`.`3vs3` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `robot1` varchar(45) NOT NULL default '',
  `robot2` varchar(45) NOT NULL default '',
  `robot3` varchar(45) NOT NULL default '',
  `status` integer unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='3vs3 cross table';

DROP TABLE IF EXISTS `crobots`.`f2f`;
CREATE TABLE  `crobots`.`f2f` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `robot1` varchar(45) NOT NULL default '',
  `robot2` varchar(45) NOT NULL default '',
  `status` integer unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='f2f cross table';

ALTER TABLE `crobots`.`f2f` ADD INDEX `Index_2`(`status`);
ALTER TABLE `crobots`.`3vs3` ADD INDEX `Index_3`(`status`);
ALTER TABLE `crobots`.`4vs4` ADD INDEX `Index_4`(`status`);

DROP TABLE IF EXISTS `crobots`.`parameters`;
CREATE TABLE  `crobots`.`parameters` (
  `id` smallint(5) unsigned NOT NULL auto_increment,
  `f2f` int(10) unsigned NOT NULL default '0',
  `4vs4` int(10) unsigned NOT NULL default '0',
  `koth` tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `crobots`.`results_4vs4`;
CREATE TABLE  `crobots`.`results_4vs4` (
  `robot` varchar(45) NOT NULL default '',
  `games` int(10) unsigned NOT NULL default '0',
  `wins` int(10) unsigned NOT NULL default '0',
  `ties` int(10) unsigned NOT NULL default '0',
  `points` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`robot`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Results of 4vs4';

DROP TABLE IF EXISTS `crobots`.`results_f2f`;
CREATE TABLE  `crobots`.`results_f2f` (
  `robot` varchar(45) NOT NULL default '',
  `games` int(10) unsigned NOT NULL default '0',
  `wins` int(10) unsigned NOT NULL default '0',
  `ties` int(10) unsigned NOT NULL default '0',
  `points` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`robot`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Results of F2F';

DROP TABLE IF EXISTS `crobots`.`robots`;
CREATE TABLE  `crobots`.`robots` (
  `id` smallint(5) unsigned NOT NULL auto_increment,
  `name` varchar(12) default NULL,
  `code` longtext character set utf8 NOT NULL,
  `author` varchar(45) NOT NULL default '''',
  `email` varchar(45) NOT NULL default '''',
  `validation` varchar(45) NOT NULL default '''',
  `creationtime` timestamp NOT NULL default '0000-00-00 00:00:00',
  `status` smallint(5) unsigned NOT NULL default '0',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `NAMEIDX` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `crobots`.`users`;
CREATE TABLE  `crobots`.`users` (
  `username` varchar(45) NOT NULL default '',
  `password` varchar(45) NOT NULL default '',
  `status` int(11) NOT NULL default '0',
  PRIMARY KEY  (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='users table';

DROP PROCEDURE IF EXISTS `crobots`.`pSelect4vs4`;

DELIMITER $$

DELIMITER $$

DROP PROCEDURE IF EXISTS `crobots`.`pSelectF2F` $$
CREATE DEFINER=`crobots`@`localhost` PROCEDURE `pSelectF2F`(
IN buffersize INTEGER -- Unused
)
BEGIN
SET @CurrentID = 1+FLOOR(2147483646*RAND());
START TRANSACTION;
UPDATE f2f SET status=@CurrentID WHERE status=0 LIMIT 10;
COMMIT;
SELECT id, robot1, robot2 FROM f2f WHERE status=@CurrentID;
END $$


DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS `crobots`.`pSelect3vs3` $$
CREATE DEFINER=`crobots`@`localhost` PROCEDURE `pSelect3vs3`(
IN buffersize INTEGER
)
BEGIN
SET @CurrentID = 1+FLOOR(2147483646*RAND());
START TRANSACTION;
UPDATE 3vs3 SET status=@CurrentID WHERE status=0 LIMIT 10;
COMMIT;
SELECT id, robot1, robot2, robot3 FROM 3vs3 WHERE status=@CurrentID;
END $$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS `crobots`.`pSelect4vs4` $$
CREATE DEFINER=`crobots`@`localhost` PROCEDURE `pSelect4vs4`(
IN buffersize INTEGER
)
BEGIN
SET @CurrentID = 1+FLOOR(2147483646*RAND());
START TRANSACTION;
UPDATE 4vs4 SET status=@CurrentID WHERE status=0 LIMIT 10;
COMMIT;
SELECT id, robot1, robot2, robot3, robot4 FROM 4vs4 WHERE status=@CurrentID;
END $$

DELIMITER ;

DROP PROCEDURE IF EXISTS `crobots`.`pTest`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`crobots`@`localhost` PROCEDURE  `crobots`.`pTest`()
BEGIN
  SELECT now();
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

DELIMITER $$
CREATE DEFINER=`crobots`@`%` PROCEDURE `pSetupResultsF2F`()
BEGIN
TRUNCATE TABLE results_f2f;
INSERT INTO results_f2f(robot)
SELECT lower(LEFT(name,character_length(name)-2)) AS robot
FROM robots
WHERE status=2;
COMMIT;
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`crobots`@`%` PROCEDURE `pSetupResults3VS3`()
BEGIN
TRUNCATE TABLE results_3vs3;
INSERT INTO results_3vs3(robot)
SELECT lower(LEFT(name,character_length(name)-2)) AS robot
FROM robots
WHERE status=2;
COMMIT;
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`crobots`@`%` PROCEDURE `pSetupResults4VS4`()
BEGIN
TRUNCATE TABLE results_4vs4;
INSERT INTO results_4vs4(robot)
SELECT lower(LEFT(name,character_length(name)-2)) AS robot
FROM robots
WHERE status=2;
COMMIT;
END$$
DELIMITER ;


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
