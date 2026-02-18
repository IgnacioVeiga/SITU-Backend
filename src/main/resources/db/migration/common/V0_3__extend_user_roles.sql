DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type t
                 JOIN pg_enum e ON t.oid = e.enumtypid
        WHERE t.typname = 'user_role'
          AND e.enumlabel = 'SUPERVISOR'
    ) THEN
        ALTER TYPE user_role ADD VALUE 'SUPERVISOR';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_type t
                 JOIN pg_enum e ON t.oid = e.enumtypid
        WHERE t.typname = 'user_role'
          AND e.enumlabel = 'EMPLOYEE'
    ) THEN
        ALTER TYPE user_role ADD VALUE 'EMPLOYEE';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_type t
                 JOIN pg_enum e ON t.oid = e.enumtypid
        WHERE t.typname = 'user_role'
          AND e.enumlabel = 'PASSENGER'
    ) THEN
        ALTER TYPE user_role ADD VALUE 'PASSENGER';
    END IF;
END
$$;
