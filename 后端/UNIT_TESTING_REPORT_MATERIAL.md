# Unit Testing Report Material

## 5.1 Unit Testing

Unit testing was carried out on **May 8, 2026**, during backend development and before final integration testing and acceptance testing. The purpose was to verify the correctness of business rules, controller delegation logic, data mapping, validation rules, workflow state transitions, and legacy data migration behavior in isolated units.

### Test Objective

The unit testing activity aimed to confirm that:

1. contributor application workflows behave correctly,
2. heritage detail and comment workflows enforce validation rules,
3. user registration, login, and profile management work as designed,
4. resource draft, publishing, rejection, archiving, and upload workflows are correct,
5. community post display, interaction, and comment logic are reliable,
6. controller endpoints delegate to the correct service methods and return expected response objects,
7. legacy community post migration logic behaves safely during initialization.

### Test Environment

- OS: Windows
- Language: Java 17
- Framework: Spring Boot 3.2.5
- Build tool: Maven Wrapper (`mvnw.cmd`)
- Test framework: JUnit 5
- Mocking framework: Mockito
- Coverage tool: JaCoCo 0.8.12

### Test Process

1. Core backend modules were reviewed to identify business-critical branches and failure conditions.
2. Unit tests were written mainly at the service layer, with additional controller, repository-adapter, and initializer tests where those classes contained observable logic.
3. Dependencies such as repositories and mappers were mocked so that each test verified only the logic of the target unit.
4. Representative normal, invalid, and boundary input data was designed for each workflow.
5. The complete unit test suite was executed with:

```powershell
.\mvnw.cmd -f <backend-module>/pom.xml test
```

6. Maven Surefire reports and JaCoCo reports were reviewed as testing evidence.

### Unit Test Scope

The completed unit tests covered the following modules:

#### Service Layer

- `ContributorApplicationService`
- `HeritageItemService`
- `WorkflowServiceImpl`
- `UserService`
- `DraftService`
- `AdminService`
- `HeritageDisplayService`

#### Controller Layer

- `ContributorApplicationController`
- `AdminContributorController`
- `HeritageItemController`
- `HeritageDisplayController`
- `UserController`
- `LppAdminController`
- `LppContributorController`
- `WorkflowController`
- `FrontendController`

#### Additional Logic Units

- `LegacyCommunityFeedInitializer`
- `HeritageDisplayRepository`
- `UserRepository` adapter (`zyl_project.zyl_login`)

#### Bootstrap and Configuration Units

- `Cpt202Application`
- `StaticResourceCacheConfig`

### Test Data Design

Representative unit test data included:

| Area | Example Test Data | Purpose |
| --- | --- | --- |
| Contributor application | normal user, admin reviewer, duplicate pending application | Verify permission control and workflow status changes |
| Heritage details | approved item, unavailable item, missing contributor, nested comments | Verify detail display, validation, view count increment, and comment tree generation |
| User management | duplicate username/email, email login, invalid password, profile updates | Verify account validation and mapping logic |
| Draft/resource management | draft owner mismatch, rejected draft resubmission, approved upload | Verify ownership checks and status transitions |
| Admin review/archive | missing resource, non-admin delete, archive record persistence | Verify admin-only operations and audit data creation |
| Community posts | invalid media, like/unlike, share count, reply target mismatch | Verify interaction logic and comment validation |
| Legacy migration | integration post archival, legacy post migration, orphan community post deletion | Verify startup migration safety |

### Implemented Test Classes and Results

| Test Class | Tests Run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| `WorkflowControllerTest` | 10 | 0 | 0 | 0 |
| `WorkflowServiceImplTest` | 23 | 0 | 0 | 0 |
| `FrontendControllerTest` | 1 | 0 | 0 | 0 |
| `LegacyCommunityFeedInitializerTest` | 3 | 0 | 0 | 0 |
| `StaticResourceCacheConfigTest` | 1 | 0 | 0 | 0 |
| `AdminServiceTest` | 14 | 0 | 0 | 0 |
| `DraftServiceTest` | 22 | 0 | 0 | 0 |
| `LppAdminControllerTest` | 7 | 0 | 0 | 0 |
| `LppContributorControllerTest` | 7 | 0 | 0 | 0 |
| `AdminContributorControllerTest` | 4 | 0 | 0 | 0 |
| `ContributorApplicationControllerTest` | 2 | 0 | 0 | 0 |
| `Cpt202ApplicationTest` | 1 | 0 | 0 | 0 |
| `ContributorApplicationServiceTest` | 21 | 0 | 0 | 0 |
| `HeritageItemControllerTest` | 2 | 0 | 0 | 0 |
| `HeritageItemServiceTest` | 13 | 0 | 0 | 0 |
| `HeritageDisplayControllerTest` | 13 | 0 | 0 | 0 |
| `HeritageDisplayRepositoryTest` | 8 | 0 | 0 | 0 |
| `HeritageDisplayServiceTest` | 16 | 0 | 0 | 0 |
| `UserControllerTest` | 6 | 0 | 0 | 0 |
| `UserRepositoryTest` | 3 | 0 | 0 | 0 |
| `UserServiceTest` | 20 | 0 | 0 | 0 |
| **Total** | **197** | **0** | **0** | **0** |

### Coverage Summary

JaCoCo coverage was generated after the test run. The overall backend unit test coverage summary was:

| Metric | Coverage |
| --- | ---: |
| Class coverage | 100.0% (53/53) |
| Method coverage | 90.1% (530/588) |
| Line coverage | 87.9% (1627/1852) |
| Instruction coverage | 87.6% (5909/6745) |
| Branch coverage | 64.5% (338/524) |
| Complexity coverage | 75.5% (645/854) |

All 53 compiled backend classes were exercised by the unit test suite. Lower branch coverage compared with class coverage is mainly caused by defensive branches and fallback paths in utility-heavy service methods, especially media summarization and validation logic.

### Key Behaviors Verified

The finished unit test suite verified the following:

- normal users can submit contributor applications,
- duplicate pending contributor applications are rejected,
- non-admin reviewers cannot approve contributor applications,
- contributor approval promotes the applicant role correctly,
- contributor revocation returns the role to `USER`,
- only approved heritage items can be viewed or commented on,
- heritage detail retrieval increments view count and builds nested comment trees,
- invalid comment submissions are rejected,
- user registration rejects duplicate usernames and emails,
- user login supports username and email identifiers,
- profile updates reject duplicate usernames and emails,
- drafts can only be edited, submitted, or deleted under valid ownership and status conditions,
- approved resources can be uploaded to the platform,
- admin publish/reject/archive/delete operations behave correctly,
- community post creation validates media input and trims content,
- like/unlike/share/comment operations update post state correctly,
- display aggregation and summary endpoints enrich returned data correctly,
- controller methods return expected response bodies and statuses,
- legacy community posts are archived, migrated, or deleted according to the migration rules.

### Defect Tracking and Resolution

Unit testing was used as an early defect-prevention and defect-verification activity. During test design, business rules were translated into explicit expected behaviors for both success and failure paths. This made it possible to verify that:

- ownership validation is consistently enforced,
- status transitions only occur in legal states,
- invalid review and comment operations fail fast,
- controller methods delegate to the correct services,
- data mapping from entities to response objects remains consistent,
- migration logic does not leave orphan legacy content in an unsafe state.

Testing also highlighted a design consistency issue already visible in the codebase: some display/recommendation DTOs use naming such as `name` while related domain entities use `title`. This inconsistency does not currently break tested behavior, but it should be standardized in a later cleanup to improve maintainability.

### How Testing Informed Improvement Before Release

The unit testing outcomes improved release confidence in several ways:

1. They verified that the main backend workflows already enforce critical validation rules before integration.
2. They showed that the whole compiled backend class set is now exercised, while also identifying branch-heavy utility code as the main remaining area for deeper future coverage improvement.
3. They created a reusable automated regression suite that can be rerun after bug fixes, refactoring, or feature expansion.
4. They provided measurable coverage evidence to support report claims about testing completeness.

### Evidence and Artefacts

#### Source test files

All unit tests are under:

- `backend module / src/test/java /`

#### Maven Surefire execution results

- `project root / reports / backend-tests / surefire-reports /`

Examples:

- `project root / reports / backend-tests / surefire-reports / com.group32.cpt202.LY_contributor.service.ContributorApplicationServiceTest.txt`
- `project root / reports / backend-tests / surefire-reports / com.group32.cpt202.zyl_project.zyl_display.HeritageDisplayServiceTest.txt`
- `project root / reports / backend-tests / surefire-reports / TEST-com.group32.cpt202.zyl_project.zyl_login.UserServiceTest.xml`

#### JaCoCo coverage report

- `project root / reports / backend-tests / jacoco / index.html`
- `project root / reports / backend-tests / jacoco / jacoco.csv`
- `project root / reports / backend-tests / jacoco / jacoco.xml`
- raw execution data: `project root / reports / backend-tests / jacoco.ex`

These files can be used directly as report evidence and as screenshot sources for the testing section.
