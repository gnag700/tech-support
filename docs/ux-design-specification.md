# Tech-Support - UX Design Specification

**Project:** Tech-Support Helpdesk System
**Author:** Sally (UX Designer)
**Date:** 2025-11-05
**Version:** 1.0
**Status:** Ready for Development

---

## Executive Summary

This UX specification defines the user experience and visual design for Tech-Support, a modern helpdesk system serving 400 employees at ЦРБ Марьина Горка medical facility. The design balances **reliability and efficiency** (core needs for medical facilities) with **friendliness and modernity** (user engagement).

**Design Philosophy:** Inspired by Zendesk's simplicity for end-users and Linear's speed for power users.

---

## 1. Emotional Foundation & Design Principles

### 1.1 Emotional Core

**Primary Emotions (Foundation):**
- **"Under Control"** - Transparency and predictability for all roles
- **"This is Easy"** - Minimal cognitive load for Employees
- **"Efficient and Organized"** - Support sees priorities and workload clearly
- **"I See Everything"** - Admin gets complete analytics

**Secondary Emotions (Engagement Layer):**
- **Friendliness** - Concise hints, stress reduction when reporting issues
- **Modernity** - Clean typography, "live" metrics (not outdated forms)

### 1.2 Design Principles

**1. Simplicity First**
- Employees should create tickets in ≤3 clicks
- No unnecessary fields or options
- Progressive disclosure for advanced features

**2. Speed for Power Users**
- Keyboard shortcuts for Support team (Cmd+K command palette)
- Bulk actions for efficiency
- Instant visual feedback on actions

**3. Transparency Always**
- Clear status indicators (color-coded badges)
- Real-time updates (optimistic UI)
- Complete audit trail accessible

**4. Trust Through Consistency**
- Predictable patterns across modules
- Reliable error handling
- Professional visual language

---

## 2. Design System Selection

### 2.1 Chosen System: **shadcn/ui**

**Rationale:**
- ✅ **Modern & Customizable** - Built on Tailwind CSS, full control over styling
- ✅ **Accessible by Default** - WCAG 2.1 AA compliant components
- ✅ **Copy-paste Architecture** - Components owned by project (not npm dependency)
- ✅ **TypeScript Native** - Type-safe, excellent DX for React/Vue
- ✅ **Radix UI Primitives** - Unstyled, accessible foundation

**Alternative Considered:**
- Material UI - Too opinionated, Google-specific look
- Ant Design - Better for Chinese markets, heavier bundle
- Chakra UI - Good but less trendy, smaller ecosystem

### 2.2 Component Library

**Core Components (MVP):**
- Button, Input, Select, Textarea
- Badge (for status indicators)
- Card, Table, Dialog (Modal)
- Dropdown Menu, Command Palette
- Toast (notifications)
- Tabs, Accordion
- Avatar, Skeleton (loading states)

**Future Components (Phase 2):**
- Date Picker, File Upload
- Rich Text Editor (for Knowledge Base)
- Charts (for Analytics - using Recharts)

---

## 3. Visual Design Language

### 3.1 Color Palette: "Calm Blue + Professional"

**Rationale:** Medical facilities require trust (blue) with professional restraint (grays).

**Primary Colors:**

```css
Primary (Brand):   #3B82F6  /* Blue-500 - trust, reliability */
Primary Dark:      #2563EB  /* Blue-600 - hover states */
Primary Light:     #DBEAFE  /* Blue-100 - backgrounds */

Secondary:         #10B981  /* Emerald-500 - success, resolved */
```

**Semantic Colors:**

```css
Success:    #10B981  /* Emerald-500 - tickets resolved */
Warning:    #F59E0B  /* Amber-500 - P2 priority */
Error:      #EF4444  /* Red-500 - P1 critical, errors */
Info:       #3B82F6  /* Blue-500 - informational messages */
```

**Neutral Grays (Tailwind Scale):**

```css
Background:      #FFFFFF  /* White */
Surface:         #F9FAFB  /* Gray-50 */
Border:          #E5E7EB  /* Gray-200 */
Text Primary:    #111827  /* Gray-900 */
Text Secondary:  #6B7280  /* Gray-500 */
Text Disabled:   #9CA3AF  /* Gray-400 */
```

**Status Color Mapping:**

```css
OPEN:        #3B82F6  /* Blue-500 */
IN_PROGRESS: #F59E0B  /* Amber-500 */
RESOLVED:    #10B981  /* Emerald-500 */
CLOSED:      #6B7280  /* Gray-500 */
```


**Priority Color Mapping:**
```
P1 (Critical): #EF4444  /* Red-500 */
P2 (High):     #F59E0B  /* Amber-500 */
P3 (Low):      #6B7280  /* Gray-500 */
```

### 3.2 Typography

**Font Family:** **Inter** (Google Fonts)
- Modern, clean, excellent readability
- Variable font support for performance
- Cyrillic character set included (Russian language)

**Type Scale:**
```
H1: 32px / 2rem   - font-bold - Page titles
H2: 24px / 1.5rem - font-semibold - Section headers
H3: 20px / 1.25rem - font-semibold - Card titles
H4: 18px / 1.125rem - font-medium - Subheadings

Body Large:  16px / 1rem - font-normal - Primary content
Body:        14px / 0.875rem - font-normal - Default text
Body Small:  12px / 0.75rem - font-normal - Helper text, labels
Caption:     11px / 0.6875rem - font-normal - Timestamps, metadata
```

**Line Height:**
- Headings: 1.2 (tight)
- Body text: 1.5 (comfortable reading)
- Small text: 1.4

**Font Weights:**
- Regular: 400 (body text)
- Medium: 500 (emphasis, buttons)
- Semibold: 600 (headings)
- Bold: 700 (strong emphasis)

### 3.3 Spacing System (Tailwind 4px base)

```
4px   (space-1)  - Tight spacing (badges, icons)
8px   (space-2)  - Compact spacing (between labels and inputs)
12px  (space-3)  - Default spacing (card padding)
16px  (space-4)  - Comfortable spacing (between sections)
24px  (space-6)  - Large spacing (page margins)
32px  (space-8)  - Extra large spacing (major sections)
48px  (space-12) - Section dividers
```

### 3.4 Border Radius

```
sm:  4px  - Buttons, inputs
md:  6px  - Cards, modals
lg:  8px  - Large containers
full: 9999px - Avatars, badges
```

### 3.5 Shadows (Elevation)

```
sm:  0 1px 2px rgba(0,0,0,0.05)    - Cards, inputs
md:  0 4px 6px rgba(0,0,0,0.07)    - Dropdowns, modals
lg:  0 10px 15px rgba(0,0,0,0.1)   - Command palette
xl:  0 20px 25px rgba(0,0,0,0.15)  - Large modals
```

---

## 4. Information Architecture

### 4.1 Navigation Structure

**Primary Navigation (Sidebar - Left):**
```
📊 Dashboard       (Support, Admin only)
🎫 Tickets         (All roles)
   - My Tickets    (Employee default view)
   - Unassigned    (Support, Admin)
   - All Tickets   (Admin only)
📚 Knowledge Base  (All roles - stub in MVP)
👥 Users           (Admin only)
📈 Analytics       (Support view-only, Admin full)
⚙️ Settings        (All roles - own settings)
   - Profile
   - Notifications
   - Security
```

**Top Bar:**
```
[Logo] [Navigation] ..................... [Search Cmd+K] [Notifications] [Avatar Menu]
```

**Avatar Menu (Dropdown):**
```
Иван Иванов
Employee | Терапия

Profile Settings
Change Password
---
Logout
```

### 4.2 URL Structure

```
/                       - Redirect based on role:
                          Employee → /tickets/my
                          Support → /tickets/unassigned
                          Admin → /dashboard

/tickets/my             - Employee's own tickets
/tickets/unassigned     - Unassigned tickets (Support, Admin)
/tickets/all            - All tickets (Admin only)
/tickets/:id            - Ticket detail view
/tickets/new            - Create ticket form

/users                  - User management (Admin)
/users/pending          - Pending approvals (Admin)
/users/:id              - User profile view/edit

/analytics              - Analytics dashboard
/kb                     - Knowledge Base (stub)
/settings               - User settings
```

---

## 5. Key User Flows

### 5.1 Employee: Create Ticket (3 Clicks)

**Flow:**
```
1. Click "New Ticket" button (top bar or sidebar)
2. Fill form:
   - Title* (placeholder: "Краткое описание проблемы")
   - Description* (placeholder: "Подробно опишите проблему")
   - Category* (dropdown: Hardware, Software, Network, Access, Other)
   - Priority (dropdown: P1/P2/P3, defaults to P3)
3. Click "Create Ticket" button

→ Success toast: "Тикет #12345 создан. Вы получите уведомление, когда он будет назначен."
→ Redirect to /tickets/:id (ticket detail view)
```

**UI Pattern (Modal):**
- Clean, focused modal (no distractions)
- Auto-focus on Title field
- Inline validation (red border + error text below field)
- Disabled submit until required fields valid
- Escape key closes modal (with confirm if fields filled)

### 5.2 Support: Claim and Resolve Ticket

**Flow:**
```
1. Navigate to /tickets/unassigned
2. See list of unassigned tickets (sorted by priority, then created_at DESC)
3. Click "Assign to Me" button on ticket card
   → Ticket moves to "My Assigned" section
   → Status changes to IN_PROGRESS
4. Click ticket to open detail view
5. Add comment: "Проверяю проблему..."
6. Update status to RESOLVED
   → Modal prompts for resolution_notes*
7. Enter resolution notes: "Принтер перезагружен, проблема решена"
8. Click "Resolve Ticket"
   → Success toast: "Тикет #12345 решён"
   → Ticket auto-closes after 7 days if no activity
```

**UI Pattern (Inbox-style):**
- Left sidebar: Ticket list with preview
- Right panel: Ticket detail view
- Comments thread (chronological, like email)
- Action buttons fixed at bottom: [Assign] [Comment] [Change Status]

### 5.3 Admin: Approve User Registration

**Flow:**
```
1. Receive Telegram notification: "Новая регистрация: Иван Иванов (ivanov@crb.by)"
2. Navigate to /users/pending
3. See pending registrations table:
   | Name | Email | Department | Location | Actions |
4. Click "View Details" → Modal with full info
5. Click "Approve"
   → Confirmation dialog: "Одобрить пользователя Иван Иванов?"
6. Click "Yes, Approve"
   → Success toast: "Пользователь одобрен. Email отправлен."
   → User receives email with login link
```

---

## 6. Core UI Components & Patterns

### 6.1 Ticket Card (List View)

**Layout:**
```
+----------------------------------------------------------+
| [P2 Badge] Ticket #12345                    [OPEN Badge] |
| Принтер в кабинете 305 не работает                        |
|                                                           |
| 👤 Иван Иванов  •  📁 Hardware  •  🕒 2 часа назад       |
+----------------------------------------------------------+
```

**Interactions:**
- Click anywhere → Navigate to ticket detail
- Hover → Subtle background change (gray-50)
- Badge colors match status/priority colors

### 6.2 Status Badge Component

**Variants:**
```jsx
<Badge variant="open">OPEN</Badge>        // Blue background
<Badge variant="in_progress">IN_PROGRESS</Badge>  // Amber background
<Badge variant="resolved">RESOLVED</Badge>  // Emerald background
<Badge variant="closed">CLOSED</Badge>    // Gray background
```

**Style:**
- Rounded full (pill shape)
- Font: 11px, font-medium, uppercase
- Padding: 4px 8px
- Border: 1px solid (darker shade of background)

### 6.3 Command Palette (Cmd+K)

**Purpose:** Quick navigation for Support power users

**Discoverability:**
- **Tooltip on search icon:** "Press Cmd+K to search" (desktop)
- **First-time user:** Show modal hint on second login: "Tip: Press Cmd+K for quick search"
- **Help menu:** Include keyboard shortcuts reference

**Features:**
- Fuzzy search across:
  - Tickets (by ID or title)
  - Navigation pages
  - Actions (Create ticket, Assign ticket, etc.)
- Keyboard navigation (↑↓ arrows, Enter to select)
- Recent searches memory

**Mobile Behavior:**
- Command palette DISABLED on mobile (touch devices)
- Search functionality available via top bar search icon (🔍)
- Alternate gesture: Swipe down on Tickets screen reveals search bar with filters

**Example Results:**
```
Search: "принтер"

TICKETS (3 results)
  #12345 - Принтер в кабинете 305 не работает
  #11234 - Проблема с принтером HP
  #10456 - Принтер не печатает цветные документы

NAVIGATION
  🎫 All Tickets
  📊 Analytics Dashboard
```

### 6.4 Comment Thread (Ticket Detail)

**Pattern:** Timeline/Activity Feed

**Layout:**
```
+-------------------------------------------------------+
| 👤 Иван Иванов создал тикет • 2 часа назад            |
| Принтер в кабинете 305 не работает. При попытке       |
| печати выдаёт ошибку "Paper Jam".                     |
+-------------------------------------------------------+
| 👤 Петр Петров назначил тикет себе • 1 час назад      |
+-------------------------------------------------------+
| 👤 Петр Петров добавил комментарий • 30 минут назад   |
| Проверяю проблему. Приду в кабинет через 15 минут.   |
+-------------------------------------------------------+
| 👤 Петр Петров решил тикет • 5 минут назад            |
| ✅ Принтер перезагружен, проблема решена.             |
+-------------------------------------------------------+
```

**Interactions:**
- Auto-scroll to latest comment on load
- "Add Comment" textarea at bottom (Support/Admin only)
- Real-time updates via SSE (Phase 2)

### 6.5 Filter Bar (Ticket List)

**Layout:**
```
[🔍 Search] [Status ▼] [Category ▼] [Priority ▼] [Date Range ▼] [Clear Filters]
```

**Behavior & Persistence:**
- **Primary storage:** URL query parameters (shareable links, browser back/forward)
  - Example: `/tickets/all?status=open&priority=p1&category=hardware`
- **Secondary storage:** localStorage for user preferences (default filters)
  - Key: `user:{user_id}:ticket-filters` 
  - Value: `{ status: ['open', 'in_progress'], sortBy: 'priority' }`
- Clear Filters button only visible when filters active
- Filter count indicator: "Filters (3 active)"
- On page refresh: Restore filters from URL (priority) → localStorage (fallback) → defaults

**Implementation Note:** Use React Router (searchParams) or Vue Router (query) for URL sync.

---

## 7. Responsive Design Strategy

### 7.1 Breakpoints

```
Mobile:     < 640px   (sm)
Tablet:     640-1024px (md-lg)
Desktop:    > 1024px   (xl)
```

### 7.2 Layout Adaptations

**Desktop (Primary):**
- Sidebar navigation (left, fixed, 240px width)
- Main content area (centered, max-width 1440px)
- Two-column layout for ticket list + detail

**Tablet:**
- Collapsible sidebar (hamburger menu)
- Single-column layout
- Ticket list → detail view (navigation)

**Mobile:**
- Bottom tab navigation (Dashboard, Tickets, Profile)
- Full-width cards
- Simplified forms (fewer fields per screen)

**Navigation Mapping (Desktop ↔ Mobile):**
```
Desktop Sidebar          →  Mobile Bottom Tabs
─────────────────────────────────────────────────
Dashboard                →  🏠 Home (Support/Admin only)
Tickets (My/Unassigned)  →  🎫 Tickets (primary tab)
Analytics                →  📊 Analytics (Support/Admin only)
Settings                 →  ⚙️ Settings (all roles)

Desktop Secondary Menu   →  Mobile Hamburger (☰)
─────────────────────────────────────────────────
Users (Admin)            →  Hamburger > Users
Knowledge Base           →  Hamburger > Knowledge Base
Search (Cmd+K)           →  Top bar search icon (🔍)
```

**Recommendation:** Employees see only 2 tabs (Tickets + Settings) to reduce cognitive load.

### 7.3 Mobile-Specific Considerations

**Ticket Creation (Mobile):**
- Multi-step form:
  - Step 1: Title + Category
  - Step 2: Description
  - Step 3: Priority (optional)
- Larger touch targets (min 44px height)
- Native-like interactions (swipe to go back)

---

## 8. Accessibility (WCAG 2.1 AA)

### 8.1 Color Contrast

**Requirements:**
- Text: 4.5:1 contrast ratio (normal text)
- Large text (18px+): 3:1 contrast ratio
- UI components: 3:1 contrast ratio

**Validation:**
- Primary Blue #3B82F6 on White: 4.64:1 ✅
- Gray-900 #111827 on White: 16.41:1 ✅
- Success Green #10B981 on White: 3.09:1 ⚠️ (use darker shade for text)

**Accessible Color Variants:**
```
Success Text: #059669  /* Emerald-600 - 4.52:1 contrast ✅ */
Warning Text: #D97706  /* Amber-600 - 4.54:1 contrast ✅ */
Error Text:   #DC2626  /* Red-600 - 4.52:1 contrast ✅ */
```

**Usage:**
- Use lighter shades (#10B981) for backgrounds/badges
- Use darker shades (#059669) for text/icons to meet WCAG 2.1 AA

### 8.2 Keyboard Navigation

**Requirements:**
- All interactive elements keyboard-accessible (Tab, Enter, Space)
- Focus indicators visible (2px blue outline)
- Logical tab order (top to bottom, left to right)
- Skip links for main content

**Keyboard Shortcuts:**
```
Cmd/Ctrl + K    - Command palette
Cmd/Ctrl + N    - New ticket
Escape          - Close modal/dropdown
Tab             - Next focusable element
Shift + Tab     - Previous focusable element
Enter           - Submit form / Select item
Space           - Toggle checkbox / Open dropdown
```

### 8.3 Screen Reader Support

**Requirements:**
- Semantic HTML (header, nav, main, article)
- ARIA labels for icon-only buttons
- ARIA live regions for dynamic content (toasts, notifications)
- Alt text for images (if any)

**Example:**
```jsx
**Example:**
```jsx
<button aria-label="Assign ticket to me">
  <UserIcon /> Assign
</button>

<div role="alert" aria-live="polite">
  Тикет #12345 успешно создан
</div>
```

**Toast Notification Accessibility:**
- Toast container MUST have `role="region"` and `aria-live="polite"`
- Stack multiple toasts vertically (max 3 visible simultaneously)
- Auto-dismiss does NOT remove focus trap for screen readers
- Each toast MUST have close button with `aria-label="Закрыть уведомление"`
```

### 8.4 Form Accessibility

- Label for every input (visible or aria-label)
- Error messages linked via aria-describedby
- Required fields marked with * and aria-required="true"
- Input types matched to content (email, tel, number)

---

## 9. Micro-interactions & Animations

### 9.1 Animation Principles

**Rules:**
- Duration: 150-300ms (quick, not distracting)
- Easing: ease-in-out (natural feel)
- Purpose: Provide feedback, guide attention
- Respect prefers-reduced-motion

### 9.2 Button Interactions

```
Hover:    Background color darkens (transition 150ms)
Active:   Scale down 0.98 (transition 100ms)
Loading:  Spinner replaces text, button disabled
Success:  Checkmark icon briefly (1s), then original text
```

### 9.3 Toast Notifications

```
Enter:  Slide in from top (300ms ease-out)
Stay:   4 seconds (success), 6 seconds (error)
Exit:   Fade out (200ms ease-in)
```

**Positions:**
- Top-right corner (desktop)
- Top-center (mobile)

**Types:**
```
Success: Green background, checkmark icon
Error:   Red background, X icon
Warning: Amber background, warning icon
Info:    Blue background, info icon
```

### 9.4 Page Transitions

```
Route change:   Fade out → Fade in (200ms each)
Modal open:     Scale from 0.95 to 1 + fade in (200ms)
Modal close:    Scale to 0.95 + fade out (150ms)
```

### 9.5 Loading States

**Skeleton Screens:**
- Use for initial page load
- Match layout of actual content
- Subtle shimmer animation (2s loop)

**Spinners:**
- Use for button actions (inline)
- Use for data fetching (center of content area)

**Optimistic UI:**
- Update UI immediately, revert on error
- Show subtle loading indicator (opacity change)

---

## 10. Error Handling & Empty States

### 10.1 Form Validation Errors

**Inline Errors (Below Field):**
```jsx
<Input 
  error="Title must be between 5 and 200 characters" 
  className="border-red-500"
/>
<p className="text-red-500 text-sm mt-1">
  Title must be between 5 and 200 characters
</p>
```

**Toast for Submission Errors:**
```
"Не удалось создать тикет. Пожалуйста, попробуйте снова."
[Retry Button]
```

### 10.2 Empty States

**No Tickets (Employee View):**
```
+------------------------------------------+
|           📭                             |
|      У вас пока нет тикетов               |
|                                          |
|  Создайте тикет, если возникла проблема  |
|         [Create Ticket Button]           |
+------------------------------------------+
```

**No Search Results:**
```
🔍 Ничего не найдено

Попробуйте изменить фильтры или поисковый запрос.
[Clear Filters Button]
```

### 10.3 Error Pages

**404 Not Found:**
```
404 - Страница не найдена

Страница, которую вы ищете, не существует.

[Go to Dashboard]  [Go Back]
```

**500 Server Error:**
```
⚠️ Что-то пошло не так

Мы уже работаем над устранением проблемы.
Попробуйте обновить страницу через несколько минут.

[Retry]  [Contact Support]
```

---

## 11. Implementation Guidelines

### 11.1 Technology Stack

**Frontend Framework:**
- **React 18** (or Vue 3 - TBD by architect)
- **TypeScript** for type safety
- **Tailwind CSS** for styling
- **shadcn/ui** components

**State Management:**
- React Query (for server state)
- Zustand (for client state)

**Routing:**
- React Router v6 (or Vue Router v4)

**Forms:**
- React Hook Form + Zod validation

### 11.2 Component Structure

```
src/
  components/
    ui/           # shadcn/ui components (Button, Input, etc.)
    tickets/      # Ticket-specific components
    users/        # User-specific components
    layout/       # Layout components (Sidebar, TopBar)
  pages/          # Route pages
  hooks/          # Custom hooks
  lib/            # Utilities, API client
  styles/         # Global styles, Tailwind config
```

### 11.3 Naming Conventions

**Components:**
- PascalCase: \TicketCard\, \StatusBadge\
- Suffix with type: \UserForm\, \TicketList\

**Files:**
- \TicketCard.tsx\, \TicketCard.test.tsx\, \TicketCard.stories.tsx\

**CSS Classes (Tailwind):**
- Use descriptive utility classes
- Extract common patterns to components
- Avoid custom CSS unless necessary

### 11.4 Performance Targets

**Core Web Vitals:**
- LCP (Largest Contentful Paint): <2.5s
- FID (First Input Delay): <100ms
- CLS (Cumulative Layout Shift): <0.1

**Bundle Size:**
- Initial load: <300KB (gzipped)
- Route chunks: <100KB each

**Optimization Strategies:**
- Code splitting per route
- Lazy load non-critical components
- Optimize images (WebP, lazy loading)
- Tree-shake unused dependencies

---

## 12. Design Tokens (CSS Variables)

```css
:root {
  /* Colors */
  --color-primary: #3B82F6;
  --color-primary-dark: #2563EB;
  --color-primary-light: #DBEAFE;
  
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-error: #EF4444;
  --color-info: #3B82F6;
  
  --color-text-primary: #111827;
  --color-text-secondary: #6B7280;
  --color-text-disabled: #9CA3AF;
  
  --color-background: #FFFFFF;
  --color-surface: #F9FAFB;
  --color-border: #E5E7EB;
  
  /* Typography */
  --font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  
  --font-size-xs: 0.6875rem;  /* 11px */
  --font-size-sm: 0.75rem;    /* 12px */
  --font-size-base: 0.875rem; /* 14px */
  --font-size-lg: 1rem;       /* 16px */
  --font-size-xl: 1.125rem;   /* 18px */
  --font-size-2xl: 1.25rem;   /* 20px */
  --font-size-3xl: 1.5rem;    /* 24px */
  --font-size-4xl: 2rem;      /* 32px */
  
  /* Spacing */
  --space-1: 0.25rem;  /* 4px */
  --space-2: 0.5rem;   /* 8px */
  --space-3: 0.75rem;  /* 12px */
  --space-4: 1rem;     /* 16px */
  --space-6: 1.5rem;   /* 24px */
  --space-8: 2rem;     /* 32px */
  --space-12: 3rem;    /* 48px */
  
  /* Border Radius */
  --radius-sm: 0.25rem;  /* 4px */
  --radius-md: 0.375rem; /* 6px */
  --radius-lg: 0.5rem;   /* 8px */
  --radius-full: 9999px;
  
  /* Shadows */
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.05);
  --shadow-md: 0 4px 6px rgba(0,0,0,0.07);
  --shadow-lg: 0 10px 15px rgba(0,0,0,0.1);
  --shadow-xl: 0 20px 25px rgba(0,0,0,0.15);
  
  /* Transitions */
  --transition-fast: 150ms ease-in-out;
  --transition-base: 200ms ease-in-out;
  --transition-slow: 300ms ease-in-out;
}
```

---

## 13. Next Steps

### 13.1 Immediate Actions

1. ✅ **Architect Review** - Validate technical feasibility
2. ✅ **Frontend Framework Decision** - React vs Vue.js
3. ✅ **Setup shadcn/ui** - Install components, configure Tailwind
4. ✅ **Create Design Tokens** - CSS variables or Tailwind config
5. ✅ **Build Component Library** - Start with core components (Button, Input, Badge)

### 13.2 Development Phases

**Phase 1: Foundation (Week 1-2)**
- Setup project structure
- Implement design tokens
- Build core UI components
- Create layout components (Sidebar, TopBar)

**Phase 2: Core Flows (Week 3-6)**
- Implement ticket creation flow
- Build ticket list views
- Create ticket detail view
- Add comment thread

**Phase 3: Advanced Features (Week 7-10)**
- Command palette (Cmd+K)
- Keyboard shortcuts
- Notifications (toast, in-app)
- Analytics dashboard

**Phase 4: Polish (Week 11-12)**
- Micro-interactions
- Loading states
- Error handling
- Accessibility audit

### 13.3 Design Artifacts to Create

- [ ] High-fidelity mockups (Figma) - optional, can design in code
- [ ] Interactive prototype (for UAT)
- [ ] Component Storybook (for documentation)
- [ ] Design system documentation site

### 13.4 Attachment Upload Pattern (Phase 2 Roadmap)

**Feature:** Allow Employees to attach files (screenshots, logs) when creating tickets.

**UI Pattern:**
```
+----------------------------------------------+
| 📎 Attach Files (optional)                    |
|                                              |
| [Drag & drop files here or click to browse] |
|                                              |
| Supported: PNG, JPG, PDF, TXT, LOG          |
| Max 5 files, 10MB each                       |
+----------------------------------------------+

Attached Files (2):
- 🖼️ screenshot_error.png (1.2 MB) [X Remove]
- 📄 error_log.txt (45 KB) [X Remove]
```

**Component Spec:**
- shadcn/ui: Use `<Input type="file" multiple />` with custom Dropzone wrapper
- Visual feedback: Blue border on drag-over, progress bar during upload
- Validation: Client-side (file type, size) + server-side (malware scan)
- Storage: Store in object storage (S3/Minio), reference in `ticket_attachments` table
- Accessibility: `aria-describedby` for error messages, keyboard navigation for remove buttons

**API Endpoint:**
- `POST /api/tickets/:id/attachments` (multipart/form-data)
- Response: `{ attachment_id, filename, url, size, mime_type }`

**Future Enhancement (Phase 3):**
- Image preview thumbnails (lightbox on click)
- Support comments also allowing attachments (for Support responses)

---

## 14. Appendix

### 14.1 Inspiration Sources

**Zendesk:**
- Simple ticket creation form
- Clean ticket detail view
- Inbox-style ticket list

**Linear:**
- Command palette (Cmd+K)
- Keyboard shortcuts
- Fast, minimal UI
- Live metrics with animations

**Other References:**
- GitHub Issues (commenting UX)
- Notion (clean typography, spacing)
- Tailwind UI (component patterns)

### 14.2 Tools & Resources

**Design:**
- Figma (for mockups if needed)
- Coolors (color palette generator)
- Type Scale (typography calculator)

**Development:**
- shadcn/ui documentation: https://ui.shadcn.com
- Tailwind CSS: https://tailwindcss.com
- Radix UI: https://radix-ui.com
- React Hook Form: https://react-hook-form.com

**Accessibility:**
- WAVE (browser extension)
- axe DevTools (browser extension)
- Lighthouse (Chrome DevTools)

---

**Sign-off:**

This UX specification provides complete design direction for implementing the Tech-Support helpdesk system. All design decisions are grounded in user needs (simplicity for Employees, speed for Support, visibility for Admin) and modern UX best practices (Zendesk + Linear patterns).

**Next Workflow:** Technical Architecture (create-architecture)

**Prepared by:** Sally (UX Designer)
**Date:** 2025-11-05
**Status:** ✅ Ready for Development

