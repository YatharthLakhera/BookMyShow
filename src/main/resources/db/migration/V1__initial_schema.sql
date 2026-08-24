-- V1: baseline schema for the movie_booking database.
--
-- Migrations are versioned, forward-only and immutable: once this file has been applied
-- anywhere it must never be edited. Schema changes arrive as new V2, V3 ... files.
-- Nothing here drops or truncates a table - the application used to recreate the whole
-- schema on every startup, which destroyed all data on every restart and deploy.
--
-- Tables are created parent-first so every foreign key has its target already in place.

CREATE TABLE `TBL_Cinema` (
  `cinema_id` int(11) NOT NULL AUTO_INCREMENT,
  `cinema_name` varchar(11) NOT NULL,
  `extraData` json DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_open` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`cinema_id`),
  UNIQUE KEY `cinema_name` (`cinema_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `TBL_Customer` (
  `customer_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `email` varchar(128) NOT NULL,
  `password` varchar(128) NOT NULL,
  `role` enum('USER','ADMIN') NOT NULL DEFAULT 'USER',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `TBL_Customer_uk_1` (`name`,`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `TBL_StatusMaster` (
  `status_id` int(11) NOT NULL AUTO_INCREMENT,
  `status` varchar(32) NOT NULL,
  PRIMARY KEY (`status_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `TBL_Show` (
  `show_id` int(11) NOT NULL AUTO_INCREMENT,
  `show_type` enum('MOVIE','IPL') NOT NULL DEFAULT 'MOVIE',
  `show_name` varchar(256) NOT NULL,
  `show_details` json DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`show_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `TBL_Hall` (
  `hall_id` int(11) NOT NULL AUTO_INCREMENT,
  `hall_code` varchar(8) NOT NULL,
  `cinema_id` int(11) NOT NULL,
  `hall_row_count` int(11) NOT NULL,
  `hall_col_count` int(11) NOT NULL,
  `is_available` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`hall_id`),
  UNIQUE KEY `TBL_Hall_hall_code_cinema_id_uk_1` (`hall_code`,`cinema_id`),
  KEY `TBL_Hall_cinema_id_fk_1` (`cinema_id`),
  CONSTRAINT `TBL_Hall_cinema_id_fk_1` FOREIGN KEY (`cinema_id`) REFERENCES `TBL_Cinema` (`cinema_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `TBL_Seat` (
  `seat_id` int(11) NOT NULL AUTO_INCREMENT,
  `seat_code` varchar(8) NOT NULL,
  `hall_id` int(11) NOT NULL,
  `seat_row_loc` int(11) NOT NULL,
  `seat_col_loc` int(11) NOT NULL,
  `extraData` json DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`seat_id`),
  UNIQUE KEY `TBL_Seat_hall_id_seat_code_uk_1` (`hall_id`,`seat_code`),
  CONSTRAINT `TBL_Seat_cinema_id` FOREIGN KEY (`hall_id`) REFERENCES `TBL_Hall` (`hall_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `TBL_LiveShow` (
  `live_show_id` int(11) NOT NULL AUTO_INCREMENT,
  `show_id` int(11) NOT NULL,
  `hall_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`live_show_id`),
  UNIQUE KEY `TBL_LiveShow_uk_1` (`show_id`,`hall_id`),
  KEY `TBL_LiveShow_hall_id_fk_1` (`hall_id`),
  CONSTRAINT `TBL_LiveShow_hall_id_fk_1` FOREIGN KEY (`hall_id`) REFERENCES `TBL_Hall` (`hall_id`),
  CONSTRAINT `TBL_LiveShow_show_id_fk_2` FOREIGN KEY (`show_id`) REFERENCES `TBL_Show` (`show_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `TBL_ScheduledLiveShow` (
  `scheduled_live_show_id` int(11) NOT NULL AUTO_INCREMENT,
  `live_show_id` int(11) NOT NULL,
  `show_start_time` datetime NOT NULL,
  `show_end_time` datetime NOT NULL,
  `tickets_price` int(11) NOT NULL,  -- Amount is in paisa
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`scheduled_live_show_id`),
  UNIQUE KEY `TBL_ScheduledLiveShow_uk_1` (`live_show_id`,`show_start_time`,`show_end_time`),
  CONSTRAINT `TBL_ScheduledLiveShow_live_show_id_fk_1` FOREIGN KEY (`live_show_id`) REFERENCES `TBL_LiveShow` (`live_show_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `TBL_ShowBooking` (
  `booking_id` int(11) NOT NULL AUTO_INCREMENT,
  `customer_id` int(11) NOT NULL,
  `scheduled_live_show_id` int(11) NOT NULL,
  `total_seats_booked` int(11) NOT NULL,
  `convenience_fee` int(11) NOT NULL DEFAULT '0',
  `total_booking_amount` int(11) NOT NULL, -- Amount is in paisa
  `booking_ref_no` varchar(32) NOT NULL,
  `status_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`booking_id`),
  UNIQUE KEY `booking_ref_no` (`booking_ref_no`),
  KEY `TBL_ShowBooking_live_show_id_fk_1` (`scheduled_live_show_id`),
  KEY `TBL_ShowBooking_status_id_fk_2` (`status_id`),
  KEY `TBL_ShowBooking_customer_id_fk_3` (`customer_id`),
  CONSTRAINT `TBL_ShowBooking_customer_id_fk_3` FOREIGN KEY (`customer_id`) REFERENCES `TBL_Customer` (`customer_id`),
  CONSTRAINT `TBL_ShowBooking_live_show_id_fk_1` FOREIGN KEY (`scheduled_live_show_id`) REFERENCES `TBL_ScheduledLiveShow` (`scheduled_live_show_id`),
  CONSTRAINT `TBL_ShowBooking_status_id_fk_2` FOREIGN KEY (`status_id`) REFERENCES `TBL_StatusMaster` (`status_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `TBL_SeatsBooking` (
  `seat_booking_id` int(11) NOT NULL AUTO_INCREMENT,
  `seat_id` int(11) NOT NULL,
  `booking_id` int(11) NOT NULL,
  `scheduled_live_show_id` int(11) NOT NULL,
  `seat_booking_status` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`seat_booking_id`),
  UNIQUE KEY `TBL_SeatsBooking_uk_1` (`seat_id`,`scheduled_live_show_id`),
  KEY `TBL_SeatsBooking_booking_id_fk_1` (`booking_id`),
  KEY `TBL_SeatsBooking_seat_booking_status_fk_1` (`seat_booking_status`),
  CONSTRAINT `TBL_SeatsBooking_booking_id_fk_1` FOREIGN KEY (`booking_id`) REFERENCES `TBL_ShowBooking` (`booking_id`),
  CONSTRAINT `TBL_SeatsBooking_booking_id_fk_2` FOREIGN KEY (`scheduled_live_show_id`) REFERENCES `TBL_ScheduledLiveShow` (`scheduled_live_show_id`),
  CONSTRAINT `TBL_SeatsBooking_seat_booking_status_fk_3` FOREIGN KEY (`seat_booking_status`) REFERENCES `TBL_StatusMaster` (`status_id`),
  CONSTRAINT `TBL_SeatsBooking_seat_id_fk_4` FOREIGN KEY (`seat_id`) REFERENCES `TBL_Seat` (`seat_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `TBL_ShowBookingStatus` (
  `show_booking_status_id` int(11) NOT NULL AUTO_INCREMENT,
  `booking_id` int(11) NOT NULL,
  `status_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`show_booking_status_id`),
  UNIQUE KEY `TBL_ShowBookingStatus_booking_id_status_uk_1` (`booking_id`,`status_id`),
  KEY `TBL_ShowBookingStatus_status_id_fk_2` (`status_id`),
  CONSTRAINT `TBL_ShowBookingStatus_booking_id_fk_2` FOREIGN KEY (`booking_id`) REFERENCES `TBL_ShowBooking` (`booking_id`),
  CONSTRAINT `TBL_ShowBookingStatus_status_id_fk_2` FOREIGN KEY (`status_id`) REFERENCES `TBL_StatusMaster` (`status_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

