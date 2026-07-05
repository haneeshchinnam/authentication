CREATE TABLE IF NOT EXISTS print_jobs (
  id uuid PRIMARY KEY,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE, 
  deleted boolean DEFAULT false,
  last_modified_at timestamp,
  last_modified_by BIGINT REFERENCES users(id) ON DELETE CASCADE,
  total_price_cents int NOT NULL,
  payment_status varchar(50) NOT NULL,
  overall_status varchar(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS job_items (
  id uuid PRIMARY KEY,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,
  job_id uuid NOT NULL REFERENCES print_jobs(id) ON DELETE CASCADE,
  deleted boolean DEFAULT false,
  last_modified_at timestamp,
  last_modified_by BIGINT REFERENCES users(id) ON DELETE CASCADE,
  file_path TEXT NOT NULL CONSTRAINT uk_jobs_items_file_path UNIQUE,
  print_type varchar(50) NOT NULL,
  unit_price_cents int NOT NULL,
  page_count INT NOT NULL DEFAULT 1
);