import { memo } from "react";
import { CircleMarker, Popup } from "react-leaflet";
import MarkerClusterGroup from "react-leaflet-cluster";
import * as L from "leaflet";
import { StatusBadge, PriorityBadge } from "@/components/ui/Badge";
import { complaintCode, formatDateTime } from "@/lib/utils";
import type { Complaint, ComplaintStatus } from "@/types";

// ---------------------------------------------------------------------------
// ComplaintMarkers — clustered individual complaint locations.
//
// Thousands of complaints are handled by MarkerClusterGroup (chunked loading,
// spiderfy on click). Clustering is disabled above zoom 14 so administrators
// can pick out individual complaints; each marker opens a glass popup with the
// full details from the already-loaded Complaint object (no extra API calls).
// ---------------------------------------------------------------------------

const STATUS_COLOR: Record<ComplaintStatus, string> = {
  PENDING: "#F59E0B",
  IN_PROGRESS: "#3B82F6",
  RESOLVED: "#10B981",
  REJECTED: "#EF4444",
};

function clusterIcon(cluster: L.MarkerCluster): L.DivIcon {
  const count = cluster.getChildCount();
  const size = count >= 100 ? "lg" : count >= 20 ? "md" : "sm";
  return L.divIcon({
    html: `<div class="hp-cluster hp-cluster-${size}">${count}</div>`,
    className: "hp-cluster-wrap",
    iconSize: L.point(44, 44),
    iconAnchor: L.point(22, 22),
  });
}

function ComplaintPopup({ complaint, onOpen }: { complaint: Complaint; onOpen: (id: number) => void }) {
  return (
    <Popup maxWidth={300} className="hp-popup" closeButton={true}>
      <div className="hp-popup-content">
        <div className="flex flex-wrap items-center gap-1.5">
          <span className="font-mono text-[11px] font-bold hp-accent">{complaintCode(complaint.id)}</span>
          <StatusBadge status={complaint.status} />
          <PriorityBadge priority={complaint.priority} />
        </div>
        <h4 className="mt-2 text-sm font-semibold leading-snug hp-text">{complaint.title}</h4>
        <p className="mt-1 line-clamp-3 text-xs leading-relaxed hp-muted">{complaint.description}</p>
        <dl className="mt-3 space-y-1 border-t pt-3 text-xs hp-muted">
          <div className="flex items-center justify-between gap-3">
            <dt>Reported</dt>
            <dd className="hp-text">{formatDateTime(complaint.createdAt)}</dd>
          </div>
          <div className="flex items-center justify-between gap-3">
            <dt>Reporter</dt>
            <dd className="hp-text">Citizen #{complaint.createdBy}</dd>
          </div>
        </dl>
        <button className="hp-popup-btn" onClick={() => onOpen(complaint.id)}>
          Open complaint
        </button>
      </div>
    </Popup>
  );
}

export const ComplaintMarkers = memo(function ComplaintMarkers({
  complaints,
  onOpenComplaint,
}: {
  complaints: Complaint[];
  onOpenComplaint: (id: number) => void;
}) {
  if (complaints.length === 0) return null;

  return (
    <MarkerClusterGroup
      chunkedLoading
      maxClusterRadius={55}
      // Keep clustering active at city zoom levels; only dissolve into
      // individual markers at close range so thousands of complaints never
      // flood the React tree at once.
      disableClusteringAtZoom={15}
      showCoverageOnHover={false}
      spiderfyOnMaxZoom={true}
      iconCreateFunction={clusterIcon}
    >
      {complaints.map((c) => (
        <CircleMarker
          key={c.id}
          center={[c.latitude, c.longitude]}
          radius={5}
          pathOptions={{
            color: "#ffffff",
            weight: 1.5,
            fillColor: STATUS_COLOR[c.status],
            fillOpacity: 0.95,
          }}
        >
          <ComplaintPopup complaint={c} onOpen={onOpenComplaint} />
        </CircleMarker>
      ))}
    </MarkerClusterGroup>
  );
});
