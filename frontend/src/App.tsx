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

import { CitizenLayout } from "@/pages/dashboard/CitizenLayout";
import { DashboardHome } from "@/pages/dashboard/DashboardHome";
import { ReportIssue } from "@/pages/dashboard/ReportIssue";
import { MyComplaints } from "@/pages/dashboard/MyComplaints";
import { ComplaintDetailPage } from "@/pages/dashboard/ComplaintDetailPage";
import { Notifications } from "@/pages/dashboard/Notifications";
import { Profile } from "@/pages/dashboard/Profile";

import { AdminLayout } from "@/pages/admin/AdminLayout";
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
            </Route>

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
              <Route path="settings" element={<SettingsPage />} />
              <Route path="community/events" element={<EventsPage />} />
              <Route path="community/articles" element={<ArticlesPage />} />
              <Route path="community/ngos" element={<NgosPage />} />
              <Route path="community/plants" element={<PlantsPage />} />
              <Route path="community/pipelines" element={<PipelinesPage />} />
              <Route path="community/lakes" element={<LakesPage />} />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
