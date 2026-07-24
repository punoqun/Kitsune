# Kitsune — Jetpack Compose Migration Plan

A phased, evidence-based plan to migrate the Android UI from **XML Views + Fragments + Navigation Component + RecyclerView/adapters** to **Jetpack Compose (Material 3)**, incrementally, keeping the app shippable at every commit.

> Baseline (verified): Kotlin 2.4.0 · AGP 9.2.1 · minSdk 26 / target 36 / compile 37 · Compose already enabled (BOM 2026.05) · Koin DI · Room · Paging 3 · Glide 5 (+ Glide-Compose) · Retrofit/OkHttp/Jackson + jsonapi-converter · Algolia InstantSearch · single-Activity app. UI surface to migrate: ~46 fragments/activities (~50–60 screens incl. 10+ bottom sheets), ~86 XML layouts, ~29 RecyclerView adapters, ~10 custom Views, 2 navigation graphs.

---

## Scope & non-goals

**In scope (UI layer only):**
- Convert screens, lists, dialogs, and bottom sheets to Compose.
- Replace RecyclerView adapters with `LazyColumn`/`LazyRow`/`LazyVerticalGrid` + `paging-compose`.
- Replace Navigation Component + Fragments with Navigation Compose (final phase).
- Port the theme (4 color variants + day/night + AMOLED + dynamic color) to a Compose Material 3 theme.

**Explicitly out of scope (unchanged):**
- Data/domain/networking: Retrofit/OkHttp/Jackson + `jsonapi-converter`.
- Persistence: Room + Paging 3 `RemoteMediator`; auth/token storage; Kotpref preferences.
- Dependency injection: Koin (only adds Compose ViewModel injection).

> The future iOS / Kotlin-&-Compose-Multiplatform effort is tracked separately in the `Kitsune-IOS` project (`Kitsune-IOS/PLAN.md`). To ease that later phase, write composables in a **portable style** — Material 3, no Android-framework types leaking into composables, ViewModels free of Android UI deps — but do **no** KMP restructuring now.

---

## Strategy — strangler-fig (stay shippable)

- **Stage A — Fragments host Compose.** Keep the existing Navigation Component graph and Fragments, but replace each Fragment's XML body with a `ComposeView` / `setContent`. Migrate screens one at a time; everything else keeps working. Adapters become Lazy lists.
- **Stage B — Navigation cutover.** Once screens are Compose, swap Navigation Component → Navigation Compose (single Compose Activity + `NavHost`), drop Fragments, move bottom sheets/dialogs into Compose, and reimplement deep links.

This is the standard incremental path: the app builds and ships at every step, and nothing half-migrated ships broken.

---

## Dependencies to add

```toml
# gradle/libs.versions.toml (additions)
androidx-navigation-compose      = "androidx.navigation:navigation-compose"
androidx-paging-compose          = "androidx.paging:paging-compose"
insert-koin-androidx-compose     = "io.insert-koin:koin-androidx-compose"
androidx-lifecycle-runtime-compose   = "androidx.lifecycle:lifecycle-runtime-compose"
androidx-lifecycle-viewmodel-compose = "androidx.lifecycle:lifecycle-viewmodel-compose"
androidx-compose-material-icons-extended = "androidx.compose.material:material-icons-extended"
# optional: me.saket.telephoto:zoomable-image (PhotoView replacement)
# optional: a Compose markdown renderer / Vico charts (else AndroidView-wrap existing)
```

Already present and reusable: `bumptech-glide-compose` (`GlideImage`), `androidx-compose-material3` (+ adaptive), `androidx-activity-compose`, `androidx-compose-ui-test`.

---

## Phase P0 — Compose foundation ★★★ (M)

**Theme.** Extend [Theme.kt](../app/src/main/java/io/github/drumber/kitsune/ui/theme/Theme.kt) into a full Compose Material 3 theme covering all four color variants (DEFAULT / PURPLE / BLUE / GREEN), light/dark, AMOLED black, and dynamic color (Material You). Source the values from [colors.xml](../app/src/main/res/values/colors.xml) and [themes.xml](../app/src/main/res/values/themes.xml). Reuse [MdcThemeAdapter.kt](../app/src/main/java/io/github/drumber/kitsune/ui/theme/MdcThemeAdapter.kt) during the transition so Compose islands inside XML screens inherit the active MDC theme.

**Hosting pattern.** Establish a `Fragment` → Compose host template (`ComposeView` + `setContent { KitsuneTheme { … } }`), plus `koinViewModel()` injection and `LiveData`/`Flow` → `collectAsStateWithLifecycle`.

**Reusable component library** (collapses the ~29 adapters + ~10 custom Views):

| Component | Replaces |
|---|---|
| `MediaCover` / `MediaItemCard` | `MediaRecyclerViewAdapter`, [MediaItemCard.kt](../app/src/main/java/io/github/drumber/kitsune/ui/component/MediaItemCard.kt) |
| `ExploreSection` (carousel) | [ExploreSection.kt](../app/src/main/java/io/github/drumber/kitsune/ui/component/ExploreSection.kt) |
| `Avatar` | `de.hdodenhof:circleimageview` |
| `RatingBar` | `me.zhanghai…materialratingbar` |
| `ExpandableText` | `at.blogc:expandabletextview` |
| `MarkdownText` (`AndroidView` + Markwon initially) | `PostContentRenderer`, `MarkdownPreviewRenderer` |
| `PagingList` + `LoadState`/empty/error | `paging` adapters, [LayoutResourceLoadingLoadState.kt](../app/src/main/java/io/github/drumber/kitsune/ui/component/LayoutResourceLoadingLoadState.kt) |
| `PullToRefreshBox` | `androidx.swiperefreshlayout` |
| `KitsuneTopAppBar` / collapsing header | toolbars, `NestedScrollableHost` |

**Verify:** previews render every theme variant; a throwaway demo screen exercises `PagingList` + `MediaCover` end-to-end.

---

## Phase P1 — Pilot slice ★★ (S–M)

Migrate a few self-contained screens to validate the patterns before scaling out:
- **Leaf screens:** [AppearanceFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/settings/AppearanceFragment.kt), [OSLibrariesFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/settings/OSLibrariesFragment.kt), [AppLogsFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/settings/AppLogsFragment.kt).
- **One paging list:** [NotificationsFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/notifications/NotificationsFragment.kt) or [FollowListFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/profile/follow/FollowListFragment.kt).

**Verify:** side-by-side parity with the old screens; existing tests green; keep them in Fragment-host mode.

---

## Phase P2 — Screen-by-screen migration ★★★ (L)

Each screen: Fragment hosts a Compose body on the existing ViewModel; then delete that screen's XML layout(s), RecyclerView adapter(s), and any now-unused custom Views. Suggested order (by dependency / value):

| Group | Screens | Notes |
|---|---|---|
| 2a Settings suite | Settings, Appearance, ThemePicker, OSS libs, App logs | finishes the pilot area |
| 2b Home / Explore | [HomeExploreFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/main/HomeExploreFragment.kt), [MediaListFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/medialist/MediaListFragment.kt) | media cards + sections |
| 2c Details | [DetailsFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/details/DetailsFragment.kt) + tabs (Episodes, Characters, MediaFeed, Reactions) + character sheet | collapsing header; image zoom |
| 2d Search | [SearchFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/search/SearchFragment.kt), [FacetFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/search/filter/FacetFragment.kt) | keep Algolia logic; Compose UI + range sliders/chips |
| 2e Library | [LibraryFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/library/LibraryFragment.kt), edit-entry, rating sheet, manage-library sheet | paging + offline; status filters |
| 2f Profile | My/User profile, About, Edit profile, Follow list | stats charts via `AndroidView`(MPAndroidChart) or Vico |
| 2g Feed | [FeedFragment.kt](../app/src/main/java/io/github/drumber/kitsune/ui/feed/FeedFragment.kt) (global/following/user/group), Post detail, Replies | `HorizontalPager` + tabs; markdown |
| 2h Create/Groups | Create post (media/unit pickers, image attach), Reaction detail, Groups, Group detail | media/unit picker sheets |
| 2i Utilities | Notifications, WebView (`AndroidView`), Photo view (zoom), remaining sheets/dialogs | |

**Verify (per screen):** parity check before deleting XML; add Compose UI tests; app still builds/ships.

---

## Phase P3 — Navigation cutover ★★★ (L)

- Build a Navigation Compose `NavHost`: bottom nav (Home / Feed / Library / Profile) + nested settings graph + global routes with typed args + deep links (`kitsune://search`, `kitsune://library`, `kitsu.app/{type}/{slug}`) + bottom-sheet/dialog destinations. Map from [main_nav_graph.xml](../app/src/main/res/navigation/main_nav_graph.xml) and [settings_nav_graph.xml](../app/src/main/res/navigation/settings_nav_graph.xml).
- Convert `MainActivity` to `setContent { KitsuneTheme { AppNavHost() } }`. Migrate the Auth and Photo-view activities into composable destinations (Onboarding is already Compose).
- Remove Fragment / SafeArgs plumbing.

**Verify:** every previous destination reachable; deep links resolve; back-stack + up-navigation behave; process-death restoration works.

---

## Phase P4 — Cleanup ★★ (M)

- Delete unused XML layouts (~86), adapters (~29), custom Views (~10), ViewBinding/DataBinding usage.
- Remove now-dead deps/plugins: SafeArgs, `navigation-fragment`/`fragment-ktx`, `viewpager2`, `swiperefreshlayout`, `circleimageview`, `materialratingbar`, `expandabletextview`. Optionally retire Markwon/MPAndroidChart if replaced by Compose-native equivalents (else keep behind `AndroidView`).
- Turn off `buildFeatures { viewBinding; dataBinding }` once unused.
- Migrate UI tests to Compose (`ui-test-junit4`); update screenshot capture (`CaptureScreenshots` / screengrab).

**Verify:** full app on Compose; all flows at parity; CI green; APK size / method count sanity check.

---

## Decisions

- Android-only; data/domain untouched.
- **Images:** keep **Glide** via the already-present Glide-Compose (`GlideImage`) — least churn.
- **Markdown / Charts:** `AndroidView`-wrap Markwon / MPAndroidChart initially (low risk); swap to Compose-native later if desired.
- **Search:** keep the Algolia logic (`SearchViewModel`, `HitsSearcher`, paging); only its UI becomes Compose.
- **Navigation:** Navigation Compose, cut over after screens are migrated.
- **Style:** portable / Compose-Multiplatform-friendly composables to ease the future iOS phase.

## Open considerations

1. **Images** — keep Glide-Compose *(recommended)* vs. switch to Coil now *(eases later CMP)*.
2. **Markdown / Charts** — `AndroidView`-wrap now *(recommended)* vs. adopt Compose-native (markdown-renderer / Vico) now.
3. **Nav timing** — Fragment-host first, single cutover in P3 *(recommended)* vs. destination-by-destination from the start.
4. **Bottom sheets** — Material 3 `ModalBottomSheet` via state *(recommended)* vs. navigation-material sheet destinations.

## Related

- iOS / Kotlin-&-Compose-Multiplatform plan: `Kitsune-IOS/PLAN.md` (builds on this Compose migration).
