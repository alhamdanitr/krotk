-- AlterTable
ALTER TABLE "plans" ADD COLUMN     "features" TEXT,
ADD COLUMN     "max_users" INTEGER NOT NULL DEFAULT 1;

-- AlterTable
ALTER TABLE "tenants" ADD COLUMN     "country" VARCHAR(50) NOT NULL DEFAULT 'Yemen',
ADD COLUMN     "currency" VARCHAR(10) NOT NULL DEFAULT 'YER',
ADD COLUMN     "logo" TEXT;
