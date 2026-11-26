-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: library_management
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Temporary view structure for view `vw_member_eligibility`
--

DROP TABLE IF EXISTS `vw_member_eligibility`;
/*!50001 DROP VIEW IF EXISTS `vw_member_eligibility`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_member_eligibility` AS SELECT 
 1 AS `person_id`,
 1 AS `member_id`,
 1 AS `full_name`,
 1 AS `email`,
 1 AS `join_date`,
 1 AS `expiry_date`,
 1 AS `account_id`,
 1 AS `account_status`,
 1 AS `is_valid_member`,
 1 AS `current_borrowed_count`,
 1 AS `remaining_borrow_quota`,
 1 AS `overdue_books_count`,
 1 AS `unpaid_fines_count`,
 1 AS `total_unpaid_amount`,
 1 AS `can_borrow`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_open_access_resources`
--

DROP TABLE IF EXISTS `vw_open_access_resources`;
/*!50001 DROP VIEW IF EXISTS `vw_open_access_resources`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_open_access_resources` AS SELECT 
 1 AS `resource_id`,
 1 AS `title`,
 1 AS `language`,
 1 AS `publisher_name`,
 1 AS `genre`,
 1 AS `publish_year`,
 1 AS `doi`,
 1 AS `license_id`,
 1 AS `license_type`,
 1 AS `license_description`,
 1 AS `can_download`,
 1 AS `can_modify`,
 1 AS `can_redistribute`,
 1 AS `commercial_use`,
 1 AS `granted_date`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_fine_details`
--

DROP TABLE IF EXISTS `vw_fine_details`;
/*!50001 DROP VIEW IF EXISTS `vw_fine_details`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_fine_details` AS SELECT 
 1 AS `loaned_copy_id`,
 1 AS `fine_id`,
 1 AS `member_id`,
 1 AS `member_name`,
 1 AS `member_email`,
 1 AS `resource_id`,
 1 AS `book_title`,
 1 AS `copy_no`,
 1 AS `loan_date`,
 1 AS `due_date`,
 1 AS `return_date`,
 1 AS `amount`,
 1 AS `reason`,
 1 AS `payment_status`,
 1 AS `paid_date`,
 1 AS `collected_by_librarian`,
 1 AS `librarian_name`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_journal_access_stats`
--

DROP TABLE IF EXISTS `vw_journal_access_stats`;
/*!50001 DROP VIEW IF EXISTS `vw_journal_access_stats`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_journal_access_stats` AS SELECT 
 1 AS `resource_id`,
 1 AS `journal_title`,
 1 AS `issn`,
 1 AS `subject_area`,
 1 AS `publisher_name`,
 1 AS `total_views`,
 1 AS `unique_readers`,
 1 AS `last_accessed_at`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_resource_catalog`
--

DROP TABLE IF EXISTS `vw_resource_catalog`;
/*!50001 DROP VIEW IF EXISTS `vw_resource_catalog`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_resource_catalog` AS SELECT 
 1 AS `resource_id`,
 1 AS `title`,
 1 AS `language`,
 1 AS `publisher_name`,
 1 AS `genre`,
 1 AS `publish_year`,
 1 AS `resource_type`,
 1 AS `standard_code`,
 1 AS `shelf_location`,
 1 AS `price`,
 1 AS `subject_area`,
 1 AS `authors`,
 1 AS `total_copies`,
 1 AS `available_copies`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_loan_history`
--

DROP TABLE IF EXISTS `vw_loan_history`;
/*!50001 DROP VIEW IF EXISTS `vw_loan_history`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_loan_history` AS SELECT 
 1 AS `loan_id`,
 1 AS `loaned_copy_id`,
 1 AS `member_id`,
 1 AS `member_name`,
 1 AS `resource_id`,
 1 AS `book_title`,
 1 AS `copy_no`,
 1 AS `loan_date`,
 1 AS `due_date`,
 1 AS `return_date`,
 1 AS `issue_librarian_id`,
 1 AS `return_librarian_id`,
 1 AS `return_state`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_active_loans`
--

DROP TABLE IF EXISTS `vw_active_loans`;
/*!50001 DROP VIEW IF EXISTS `vw_active_loans`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_active_loans` AS SELECT 
 1 AS `loan_id`,
 1 AS `loaned_copy_id`,
 1 AS `member_id`,
 1 AS `member_name`,
 1 AS `member_email`,
 1 AS `issued_by_librarian`,
 1 AS `resource_id`,
 1 AS `book_title`,
 1 AS `isbn`,
 1 AS `copy_no`,
 1 AS `book_price`,
 1 AS `loan_date`,
 1 AS `due_date`,
 1 AS `overdue_days`,
 1 AS `loan_status`,
 1 AS `estimated_overdue_fine`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `vw_member_eligibility`
--

/*!50001 DROP VIEW IF EXISTS `vw_member_eligibility`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_member_eligibility` AS select `m`.`person_id` AS `person_id`,`m`.`member_id` AS `member_id`,concat(`p`.`fname`,' ',ifnull(concat(`p`.`minit`,'. '),''),`p`.`lname`) AS `full_name`,`p`.`email` AS `email`,`m`.`join_date` AS `join_date`,`m`.`expiry_date` AS `expiry_date`,`a`.`account_id` AS `account_id`,`a`.`status` AS `account_status`,((`m`.`expiry_date` >= curdate()) and (`a`.`status` = 'Active')) AS `is_valid_member`,count(distinct (case when ((`lc`.`return_date` is null) and (`bc`.`status` = 'Borrowed')) then `lc`.`loaned_copy_id` end)) AS `current_borrowed_count`,(5 - count(distinct (case when ((`lc`.`return_date` is null) and (`bc`.`status` = 'Borrowed')) then `lc`.`loaned_copy_id` end))) AS `remaining_borrow_quota`,count(distinct (case when ((`lc`.`return_date` is null) and (`lr`.`due_date` < curdate())) then `lc`.`loaned_copy_id` end)) AS `overdue_books_count`,count(distinct (case when (`f`.`status` = 'Unpaid') then concat(`f`.`loaned_copy_id`,'-',`f`.`fine_id`) end)) AS `unpaid_fines_count`,coalesce(sum(distinct (case when (`f`.`status` = 'Unpaid') then `f`.`amount` else 0 end)),0) AS `total_unpaid_amount`,(case when ((`m`.`expiry_date` >= curdate()) and (`a`.`status` = 'Active') and (count(distinct (case when ((`lc`.`return_date` is null) and (`bc`.`status` = 'Borrowed')) then `lc`.`loaned_copy_id` end)) < 5) and (count(distinct (case when ((`lc`.`return_date` is null) and (`lr`.`due_date` < curdate())) then `lc`.`loaned_copy_id` end)) = 0) and (count(distinct (case when (`f`.`status` = 'Unpaid') then concat(`f`.`loaned_copy_id`,'-',`f`.`fine_id`) end)) = 0)) then 1 else 0 end) AS `can_borrow` from ((((((`member` `m` join `person` `p` on((`m`.`person_id` = `p`.`person_id`))) left join `account` `a` on(((`p`.`person_id` = `a`.`person_id`) and (`a`.`role` = 'Member')))) left join `loan_record` `lr` on((`m`.`member_id` = `lr`.`member_id`))) left join `loaned_copy` `lc` on((`lr`.`loan_id` = `lc`.`loan_id`))) left join `book_copy` `bc` on(((`lc`.`resource_id` = `bc`.`resource_id`) and (`lc`.`copy_no` = `bc`.`copy_no`)))) left join `fine` `f` on((`lc`.`loaned_copy_id` = `f`.`loaned_copy_id`))) group by `m`.`person_id`,`m`.`member_id`,`p`.`fname`,`p`.`minit`,`p`.`lname`,`p`.`email`,`m`.`join_date`,`m`.`expiry_date`,`a`.`account_id`,`a`.`status` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_open_access_resources`
--

/*!50001 DROP VIEW IF EXISTS `vw_open_access_resources`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_open_access_resources` AS select `r`.`resource_id` AS `resource_id`,`r`.`title` AS `title`,`r`.`language` AS `language`,`r`.`publisher_name` AS `publisher_name`,`r`.`genre` AS `genre`,`r`.`publish_year` AS `publish_year`,`oa`.`doi` AS `doi`,`l`.`license_id` AS `license_id`,`l`.`license_type` AS `license_type`,`l`.`description` AS `license_description`,`l`.`can_download` AS `can_download`,`l`.`can_modify` AS `can_modify`,`l`.`can_redistribute` AS `can_redistribute`,`l`.`commercial_use` AS `commercial_use`,`al`.`granted_date` AS `granted_date` from (((`open_access` `oa` join `resource` `r` on((`oa`.`resource_id` = `r`.`resource_id`))) join `access_license` `al` on((`oa`.`resource_id` = `al`.`resource_id`))) join `license` `l` on((`al`.`license_id` = `l`.`license_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_fine_details`
--

/*!50001 DROP VIEW IF EXISTS `vw_fine_details`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_fine_details` AS select `f`.`loaned_copy_id` AS `loaned_copy_id`,`f`.`fine_id` AS `fine_id`,`lr`.`member_id` AS `member_id`,concat(`pm`.`fname`,' ',ifnull(concat(`pm`.`minit`,'. '),''),`pm`.`lname`) AS `member_name`,`pm`.`email` AS `member_email`,`r`.`resource_id` AS `resource_id`,`r`.`title` AS `book_title`,`lc`.`copy_no` AS `copy_no`,`lr`.`loan_date` AS `loan_date`,`lr`.`due_date` AS `due_date`,`lc`.`return_date` AS `return_date`,`f`.`amount` AS `amount`,`f`.`reason` AS `reason`,`f`.`status` AS `payment_status`,`f`.`paid_date` AS `paid_date`,`f`.`librarian_id` AS `collected_by_librarian`,concat(`pl`.`fname`,' ',`pl`.`lname`) AS `librarian_name` from (((((((`fine` `f` join `loaned_copy` `lc` on((`f`.`loaned_copy_id` = `lc`.`loaned_copy_id`))) join `loan_record` `lr` on((`lc`.`loan_id` = `lr`.`loan_id`))) join `member` `m` on((`lr`.`member_id` = `m`.`member_id`))) join `person` `pm` on((`m`.`person_id` = `pm`.`person_id`))) join `resource` `r` on((`lc`.`resource_id` = `r`.`resource_id`))) left join `librarian` `l` on((`f`.`librarian_id` = `l`.`librarian_id`))) left join `person` `pl` on((`l`.`person_id` = `pl`.`person_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_journal_access_stats`
--

/*!50001 DROP VIEW IF EXISTS `vw_journal_access_stats`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_journal_access_stats` AS select `j`.`resource_id` AS `resource_id`,`r`.`title` AS `journal_title`,`j`.`issn` AS `issn`,`j`.`subject_area` AS `subject_area`,`r`.`publisher_name` AS `publisher_name`,count(`al`.`access_id`) AS `total_views`,count(distinct `al`.`member_id`) AS `unique_readers`,max(`al`.`access_time`) AS `last_accessed_at` from ((`journal` `j` join `resource` `r` on((`j`.`resource_id` = `r`.`resource_id`))) left join `access_log` `al` on((`j`.`resource_id` = `al`.`resource_id`))) group by `j`.`resource_id`,`r`.`title`,`j`.`issn`,`j`.`subject_area`,`r`.`publisher_name` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_resource_catalog`
--

/*!50001 DROP VIEW IF EXISTS `vw_resource_catalog`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_resource_catalog` AS select `r`.`resource_id` AS `resource_id`,`r`.`title` AS `title`,`r`.`language` AS `language`,`r`.`publisher_name` AS `publisher_name`,`r`.`genre` AS `genre`,`r`.`publish_year` AS `publish_year`,(case when (`pb`.`resource_id` is not null) then 'Physical_Book' when (`j`.`resource_id` is not null) then 'Journal' when (`oa`.`resource_id` is not null) then 'Open_Access' end) AS `resource_type`,coalesce(`pb`.`isbn`,`j`.`issn`,`oa`.`doi`) AS `standard_code`,`pb`.`shelf_location` AS `shelf_location`,`pb`.`price` AS `price`,`j`.`subject_area` AS `subject_area`,group_concat(distinct concat(`p`.`fname`,' ',ifnull(concat(`p`.`minit`,'. '),''),`p`.`lname`) order by `p`.`lname` ASC separator ', ') AS `authors`,count(distinct `bc`.`copy_no`) AS `total_copies`,count(distinct (case when (`bc`.`status` = 'Available') then `bc`.`copy_no` end)) AS `available_copies` from ((((((`resource` `r` left join `physical_book` `pb` on((`r`.`resource_id` = `pb`.`resource_id`))) left join `journal` `j` on((`r`.`resource_id` = `j`.`resource_id`))) left join `open_access` `oa` on((`r`.`resource_id` = `oa`.`resource_id`))) left join `book_copy` `bc` on((`pb`.`resource_id` = `bc`.`resource_id`))) left join `resource_author` `ra` on((`r`.`resource_id` = `ra`.`resource_id`))) left join `person` `p` on((`ra`.`person_id` = `p`.`person_id`))) group by `r`.`resource_id`,`r`.`title`,`r`.`language`,`r`.`publisher_name`,`r`.`genre`,`r`.`publish_year`,`pb`.`resource_id`,`j`.`resource_id`,`oa`.`resource_id`,`pb`.`isbn`,`j`.`issn`,`oa`.`doi`,`pb`.`shelf_location`,`pb`.`price`,`j`.`subject_area` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_loan_history`
--

/*!50001 DROP VIEW IF EXISTS `vw_loan_history`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_loan_history` AS select `lr`.`loan_id` AS `loan_id`,`lc`.`loaned_copy_id` AS `loaned_copy_id`,`lr`.`member_id` AS `member_id`,concat(`pm`.`fname`,' ',`pm`.`lname`) AS `member_name`,`r`.`resource_id` AS `resource_id`,`r`.`title` AS `book_title`,`lc`.`copy_no` AS `copy_no`,`lr`.`loan_date` AS `loan_date`,`lr`.`due_date` AS `due_date`,`lc`.`return_date` AS `return_date`,`lr`.`librarian_id` AS `issue_librarian_id`,`lc`.`return_librarian_id` AS `return_librarian_id`,(case when ((`lc`.`return_date` is null) and (`lr`.`due_date` < curdate())) then 'Overdue' when (`lc`.`return_date` is null) then 'Borrowing' when (`lc`.`return_date` > `lr`.`due_date`) then 'Returned Late' else 'Returned On Time' end) AS `return_state` from ((((`loan_record` `lr` join `member` `m` on((`lr`.`member_id` = `m`.`member_id`))) join `person` `pm` on((`m`.`person_id` = `pm`.`person_id`))) join `loaned_copy` `lc` on((`lr`.`loan_id` = `lc`.`loan_id`))) join `resource` `r` on((`lc`.`resource_id` = `r`.`resource_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_active_loans`
--

/*!50001 DROP VIEW IF EXISTS `vw_active_loans`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_active_loans` AS select `lr`.`loan_id` AS `loan_id`,`lc`.`loaned_copy_id` AS `loaned_copy_id`,`lr`.`member_id` AS `member_id`,concat(`pm`.`fname`,' ',`pm`.`lname`) AS `member_name`,`pm`.`email` AS `member_email`,`lr`.`librarian_id` AS `issued_by_librarian`,`lc`.`resource_id` AS `resource_id`,`r`.`title` AS `book_title`,`pb`.`isbn` AS `isbn`,`lc`.`copy_no` AS `copy_no`,`pb`.`price` AS `book_price`,`lr`.`loan_date` AS `loan_date`,`lr`.`due_date` AS `due_date`,greatest((to_days(curdate()) - to_days(`lr`.`due_date`)),0) AS `overdue_days`,(case when (`lr`.`due_date` < curdate()) then 'Overdue' else 'In Term' end) AS `loan_status`,(case when (`lr`.`due_date` < curdate()) then least(((to_days(curdate()) - to_days(`lr`.`due_date`)) * 5000),`pb`.`price`) else 0 end) AS `estimated_overdue_fine` from (((((`loan_record` `lr` join `member` `m` on((`lr`.`member_id` = `m`.`member_id`))) join `person` `pm` on((`m`.`person_id` = `pm`.`person_id`))) join `loaned_copy` `lc` on((`lr`.`loan_id` = `lc`.`loan_id`))) join `resource` `r` on((`lc`.`resource_id` = `r`.`resource_id`))) join `physical_book` `pb` on((`r`.`resource_id` = `pb`.`resource_id`))) where (`lc`.`return_date` is null) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-26 22:02:18
