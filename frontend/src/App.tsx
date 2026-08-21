import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "@/lib/auth";
import { ToastProvider } from "@/lib/toast";
import { PublicLayout } from "@/components/layout/PublicLayout";
import { RequireAdmin, RequireAuth, RequireFieldOfficer, RedirectIfAuthed } from "@/components/layout/RouteGuards";

import { Home } from "@/pages/public/Home";
import { Login } from "@/pages/public/Login";
import { Register } from "@/pages/public/Register";
import { ForgotPassword } from "@/pages/public/ForgotPassword";
import { TrackComplaint } from "@/pages/public/TrackComplaint";
import { EventsPage as PublicEventsPage } from "@/pages/public/Events";
import { ArticlesPage as PublicArticlesPage } from "@/pages/public/Articles";
import { NgosPage as PublicNgosPage } from "@/pages/public/Ngos";
import { LakesPage as PublicLakesPage } from "@/pages/public/Lakes";
import { InfrastructurePage } from "@/pages/public/Infrastructure";

import { CitizenLayout } from "@/pages/dashboard/CitizenLayout";
import { DashboardHome } from "@/pages/dashboard/DashboardHome";
import { ReportIssue } from "@/pages/dashboard/ReportIssue";
import { MyComplaints } from "@/pages/dashboard/MyComplaints";
import { ComplaintDetailPage } from "@/pages/dashboard/ComplaintDetailPage";
import { Notifications } from "@/pages/dashboard/Notifications";
import { Profile } from "@/pages/dashboard/Profile";

import { AdminLayout } from "@/pages/admin/AdminLayout";
import { AdminNgoApplications } from "@/pages/admin/AdminNgoApplications";
import { AdminProfile } from "@/pages/admin/AdminProfile";
import { HotspotMapPage } from "@/pages/admin/HotspotMapPage";
import { AdminDashboard } from "@/pages/admin/AdminDashboard";
import { ManageComplaints } from "@/pages/admin/ManageComplaints";
import { AnalyticsPage } from "@/pages/admin/Analytics";
import { SettingsPage } from "@/pages/admin/Settings";
import { ArticlesPage } from "@/pages/admin/community/Articles";
import { EventsPage } from "@/pages/admin/community/Events";
import { NgosPage } from "@/pages/admin/community/Ngos";
import { PlantsPage } from "@/pages/admin/community/Plants";
import { PipelinesPage } from "@/pages/admin/community/Pipelines";
import { LakesPage } from "@/pages/admin/community/Lakes";

import { FieldOfficerLayout } from "@/pages/officer/FieldOfficerLayout";
import { AssignedComplaints } from "@/pages/officer/AssignedComplaints";
import { FieldOfficerComplaintDetail } from "@/pages/officer/ComplaintDetailPage";

import { NgoLayout } from "@/pages/ngo/NgoLayout";
import { NgoLogin } from "@/pages/ngo/NgoLogin";
import { NgoApply } from "@/pages/ngo/NgoApply";
import { NgoDashboardPage } from "@/pages/ngo/NgoDashboard";
import { NgoProfile } from "@/pages/ngo/NgoProfile";
import { NgoEvents } from "@/pages/ngo/NgoEvents";
import { NgoDrives } from "@/pages/ngo/NgoDrives";
import { NgoAchievements } from "@/pages/ngo/NgoAchievements";
import { NgoProgressPage } from "@/pages/ngo/NgoProgress";
import { NgoFunds } from "@/pages/ngo/NgoFunds";
import { NgoParticipants } from "@/pages/ngo/NgoParticipants";
import { AiAssistantPage as NgoAiPage } from "@/pages/ngo/AiAssistantPage";
import { MyEventsPage } from "@/pages/dashboard/MyEvents";
import { RequireNgoRepresentative } from "@/components/layout/RouteGuards";
import { AiAssistantPage as CitizenAiPage } from "@/pages/dashboard/AiAssistantPage";
import { AiAssistantPage as AdminAiPage } from "@/pages/admin/AiAssistantPage";

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            {/* Public */}
            <Route element={<PublicLayout />}>
              <Route path="/" element={<Home />} />
              <Route
                path="/login"
                element={
                  <RedirectIfAuthed>
                    <Login />
                  </RedirectIfAuthed>
                }
              />
              <Route
                path="/register"
                element={
                  <RedirectIfAuthed>
                    <Register />
                  </RedirectIfAuthed>
                }
              />
              <Route
                path="/forgot-password"
                element={
                  <RedirectIfAuthed>
                    <ForgotPassword />
                  </RedirectIfAuthed>
                }
              />
              <Route path="/track" element={<TrackComplaint />} />
              <Route path="/track/:id" element={<TrackComplaint />} />
              <Route path="/events" element={<PublicEventsPage />} />
              <Route path="/articles" element={<PublicArticlesPage />} />
              <Route path="/ngos" element={<PublicNgosPage />} />
              <Route path="/lakes" element={<PublicLakesPage />} />
            <Route path="/infrastructure" element={<InfrastructurePage />} />
          </Route>

            {/* NGO public routes */}
            <Route
              path="/ngo/login"
              element={
                <RedirectIfAuthed>
                  <NgoLogin />
                </RedirectIfAuthed>
              }
            />
            <Route
              path="/ngo/apply"
              element={<NgoApply />}
            />

            {/* Citizen dashboard */}
            <Route
              path="/dashboard"
              element={
                <RequireAuth>
                  <CitizenLayout />
                </RequireAuth>
              }
            >
              <Route index element={<DashboardHome />} />
              <Route path="report" element={<ReportIssue />} />
              <Route path="complaints" element={<MyComplaints />} />
              <Route path="complaints/:id" element={<ComplaintDetailPage />} />
              <Route path="my-events" element={<MyEventsPage />} />
              <Route path="ai" element={<CitizenAiPage />} />
              <Route path="notifications" element={<Notifications />} />
              <Route path="profile" element={<Profile />} />
            </Route>

            {/* Field officer dashboard */}
            <Route
              path="/officer"
              element={
                <RequireFieldOfficer>
                  <FieldOfficerLayout />
                </RequireFieldOfficer>
              }
            >
              <Route index element={<AssignedComplaints />} />
              <Route path="complaints" element={<AssignedComplaints />} />
              <Route path="complaints/:id" element={<FieldOfficerComplaintDetail />} />
              <Route path="notifications" element={<Notifications />} />
              <Route path="profile" element={<Profile />} />
            </Route>

            {/* Admin / Authority */}
            <Route
              path="/admin"
              element={
                <RequireAdmin>
                  <AdminLayout />
                </RequireAdmin>
              }
            >
              <Route index element={<AdminDashboard />} />
              <Route path="profile" element={<AdminProfile />} />
              <Route path="hotspots" element={<HotspotMapPage />} />
              <Route path="complaints" element={<ManageComplaints />} />
              <Route path="analytics" element={<AnalyticsPage />} />
              <Route path="ai-assistant" element={<AdminAiPage />} />
              <Route path="settings" element={<SettingsPage />} />
              <Route path="ngo-applications" element={<AdminNgoApplications />} />
              <Route path="community/events" element={<EventsPage />} />
              <Route path="community/articles" element={<ArticlesPage />} />
              <Route path="community/ngos" element={<NgosPage />} />
              <Route path="community/plants" element={<PlantsPage />} />
              <Route path="community/pipelines" element={<PipelinesPage />} />
              <Route path="community/lakes" element={<LakesPage />} />
            </Route>

            {/* NGO dashboard */}
            <Route
              path="/ngo"
              element={
                <RequireNgoRepresentative>
                  <NgoLayout />
                </RequireNgoRepresentative>
              }
            >
              <Route index element={<NgoDashboardPage />} />
              <Route path="profile" element={<NgoProfile />} />
              <Route path="events" element={<NgoEvents />} />
              <Route path="drives" element={<NgoDrives />} />
              <Route path="achievements" element={<NgoAchievements />} />
              <Route path="progress" element={<NgoProgressPage />} />
              <Route path="funds" element={<NgoFunds />} />
              <Route path="participants" element={<NgoParticipants />} />
              <Route path="ai" element={<NgoAiPage />} />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
