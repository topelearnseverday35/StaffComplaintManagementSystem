package FinalYearProject.StaffComplaintMgmtSystem.enums;

/**
 * All departments at Babcock University, grouped by their parent School/College.
 * Used to scope complaint visibility so that HODs and Deans
 * only see complaints from staff in their own department/school.
 */
public enum Department {

    // ── Joel Awoniyi School of Education & Humanities ──────────────────────
    EDUCATION_EDUCATION("Education", "School of Education & Humanities"),
    EDUCATION_ENGLISH_LITERARY_STUDIES("English & Literary Studies", "School of Education & Humanities"),
    EDUCATION_FRENCH("French", "School of Education & Humanities"),
    EDUCATION_HISTORY_INTERNATIONAL_STUDIES("History & International Studies", "School of Education & Humanities"),
    EDUCATION_MUSIC_CREATIVE_ARTS("Music & Creative Arts", "School of Education & Humanities"),
    EDUCATION_CHRISTIAN_RELIGIOUS_STUDIES("Christian Religious Studies", "School of Education & Humanities"),

    // ── School of Law & Security Studies ───────────────────────────────────
    LAW_LAW("Law", "School of Law & Security Studies"),
    LAW_INTERNATIONAL_LAW_DIPLOMACY("International Law & Diplomacy", "School of Law & Security Studies"),
    LAW_SECURITY_STUDIES("Security Studies", "School of Law & Security Studies"),

    // ── School of Management Sciences ──────────────────────────────────────
    MANAGEMENT_ACCOUNTING("Accounting", "School of Management Sciences"),
    MANAGEMENT_BANKING_FINANCE("Banking & Finance", "School of Management Sciences"),
    MANAGEMENT_BUSINESS_ADMINISTRATION("Business Administration", "School of Management Sciences"),
    MANAGEMENT_MARKETING("Marketing", "School of Management Sciences"),
    MANAGEMENT_INFORMATION_RESOURCES_MANAGEMENT("Information Resources Management", "School of Management Sciences"),
    MANAGEMENT_ENTREPRENEURSHIP("Entrepreneurship & Business Management", "School of Management Sciences"),

    // ── Veronica Adeleke School of Social Sciences ─────────────────────────
    SOCIAL_ECONOMICS("Economics", "School of Social Sciences"),
    SOCIAL_MASS_COMMUNICATION("Mass Communication", "School of Social Sciences"),
    SOCIAL_POLITICAL_SCIENCE("Political Science & Public Administration", "School of Social Sciences"),
    SOCIAL_PSYCHOLOGY("Psychology", "School of Social Sciences"),
    SOCIAL_SOCIOLOGY("Sociology", "School of Social Sciences"),
    SOCIAL_SOCIAL_WORK("Social Work", "School of Social Sciences"),

    // ── School of Computing & Engineering Sciences ──────────────────────────
    COMPUTING_COMPUTER_SCIENCE("Computer Science", "School of Computing & Engineering Sciences"),
    COMPUTING_SOFTWARE_ENGINEERING("Software Engineering", "School of Computing & Engineering Sciences"),
    COMPUTING_INFORMATION_SYSTEMS("Information Systems", "School of Computing & Engineering Sciences"),
    COMPUTING_CYBER_SECURITY("Cyber Security", "School of Computing & Engineering Sciences"),
    COMPUTING_ELECTRICAL_ELECTRONICS("Electrical & Electronics Engineering", "School of Computing & Engineering Sciences"),
    COMPUTING_COMPUTER_ENGINEERING("Computer Engineering", "School of Computing & Engineering Sciences"),
    COMPUTING_MECHATRONICS("Mechatronics Engineering", "School of Computing & Engineering Sciences"),
    COMPUTING_TELECOMMUNICATIONS("Telecommunications Engineering", "School of Computing & Engineering Sciences"),

    // ── School of Science & Technology ─────────────────────────────────────
    SCIENCE_BIOCHEMISTRY("Biochemistry", "School of Science & Technology"),
    SCIENCE_BIOLOGY("Biology", "School of Science & Technology"),
    SCIENCE_BIOTECHNOLOGY("Biotechnology", "School of Science & Technology"),
    SCIENCE_CHEMISTRY("Chemistry", "School of Science & Technology"),
    SCIENCE_FOOD_SCIENCE("Food Science & Technology", "School of Science & Technology"),
    SCIENCE_MATHEMATICS("Mathematics", "School of Science & Technology"),
    SCIENCE_MICROBIOLOGY("Microbiology", "School of Science & Technology"),
    SCIENCE_PHYSICS("Physics with Electronics", "School of Science & Technology"),
    SCIENCE_INDUSTRIAL_CHEMISTRY("Industrial Chemistry", "School of Science & Technology"),
    SCIENCE_STATISTICS("Statistics", "School of Science & Technology"),

    // ── Benjamin S. Carson College of Health & Medical Sciences ────────────
    HEALTH_MEDICINE("Medicine & Surgery", "College of Health & Medical Sciences"),
    HEALTH_ANATOMY("Anatomy", "College of Health & Medical Sciences"),
    HEALTH_PHYSIOLOGY("Physiology", "College of Health & Medical Sciences"),
    HEALTH_PHARMACOLOGY("Pharmacology", "College of Health & Medical Sciences"),
    HEALTH_MORBID_ANATOMY("Morbid Anatomy & Forensic Medicine", "College of Health & Medical Sciences"),
    HEALTH_HAEMATOLOGY("Haematology & Blood Transfusion", "College of Health & Medical Sciences"),
    HEALTH_MEDICAL_MICROBIOLOGY("Medical Microbiology & Parasitology", "College of Health & Medical Sciences"),
    HEALTH_CHEMICAL_PATHOLOGY("Chemical Pathology", "College of Health & Medical Sciences"),
    HEALTH_RADIOLOGY("Radiology", "College of Health & Medical Sciences"),
    HEALTH_PAEDIATRICS("Paediatrics", "College of Health & Medical Sciences"),
    HEALTH_OBSTETRICS("Obstetrics & Gynaecology", "College of Health & Medical Sciences"),
    HEALTH_SURGERY("Surgery", "College of Health & Medical Sciences"),
    HEALTH_MEDICINE_DEPT("Internal Medicine", "College of Health & Medical Sciences"),
    HEALTH_PSYCHIATRY("Psychiatry", "College of Health & Medical Sciences"),
    HEALTH_OPHTHALMOLOGY("Ophthalmology", "College of Health & Medical Sciences"),

    // ── School of Nursing Sciences ──────────────────────────────────────────
    NURSING_NURSING("Nursing Sciences", "School of Nursing Sciences"),

    // ── School of Public & Applied Health ───────────────────────────────────
    PUBLIC_HEALTH_PUBLIC_HEALTH("Public Health", "School of Public & Applied Health"),
    PUBLIC_HEALTH_ENVIRONMENTAL_HEALTH("Environmental Health Sciences", "School of Public & Applied Health"),
    PUBLIC_HEALTH_HEALTH_PROMOTION("Health Promotion & Education", "School of Public & Applied Health"),
    PUBLIC_HEALTH_EPIDEMIOLOGY("Epidemiology & Biostatistics", "School of Public & Applied Health"),

    // ── General / Administration ─────────────────────────────────────────────
    GENERAL_ADMINISTRATION("University Administration", "General"),
    GENERAL_LIBRARY("Library Services", "General"),
    GENERAL_CHAPEL("University Chaplaincy", "General"),
    GENERAL_ICT("ICT Services", "General");

    private final String displayName;
    private final String school;

    Department(String displayName, String school) {
        this.displayName = displayName;
        this.school      = school;
    }

    public String getDisplayName() { return displayName; }
    public String getSchool()      { return school; }
}
