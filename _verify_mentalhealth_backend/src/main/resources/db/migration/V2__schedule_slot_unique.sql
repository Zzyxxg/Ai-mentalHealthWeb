ALTER TABLE schedule_slot
  ADD UNIQUE KEY uk_counselor_date_time (counselor_user_id, date, start_time, end_time);

