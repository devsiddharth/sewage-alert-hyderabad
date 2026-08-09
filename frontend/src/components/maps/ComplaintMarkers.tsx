import { memo, useCallback, useRef, useState } from "react";
import { CircleMarker, Popup } from "react-leaflet";
import MarkerClusterGroup from "react-leaflet-cluster";
import * as L from "leaflet";
import { StatusBadge, PriorityBadge } from "@/components/ui/Badge";
import { complaintCode, formatDateTime } from "@/lib/utils";
import type { Complaint, ComplaintStatus } from "@/types";

// ---------------------------------------------------------------------------
// ComplaintMarkers — clustered individual complaint locations.
//
// Interaction model:
//   - Clusters: hovering shows a count tooltip; clicking zooms to the group
//     (default MarkerClusterGroup behaviour), dissolving into individual
//     markers at zoom ≥ 15.
//   - Individual markers: hovering opens the glass popup preview (it closes
//     again when the cursor leaves); clicking pins the popup open so its
//     "Open complaint" button is reachable — that opens the full detail modal.
//     Everything comes from the already-loaded Complaint objects, no extra
//     API calls.
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
    iconSize: L.point(36, 36),
    iconAnchor: L.point(18, 18),
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

/**
 * A single complaint point.
 *
 * Hovering previews the popup (and enlarges the point slightly); a click
 * "pins" the popup so it stays open after the cursor leaves, letting the user
 * reach the "Open complaint" action. The popup unpins when dismissed via the
 * close button or by clicking elsewhere on the map (Leaflet auto-close).
 */
function ComplaintMarker({
  complaint,
  onOpenComplaint,
}: {
  complaint: Complaint;
  onOpenComplaint: (id: number) => void;
}) {
  const markerRef = useRef<L.CircleMarker | null>(null);
  const [pinned, setPinned] = useState(false);

  return (
    <CircleMarker
      ref={markerRef}
      center={[complaint.latitude, complaint.longitude]}
      radius={6}
      pathOptions={{
        color: "#ffffff",
        weight: 1.5,
        fillColor: STATUS_COLOR[complaint.status],
        fillOpacity: 0.95,
      }}
      eventHandlers={{
        mouseover: () => {
          markerRef.current?.setRadius(8);
          if (!pinned) markerRef.current?.openPopup();
        },
        mouseout: () => {
          markerRef.current?.setRadius(6);
          if (!pinned) markerRef.current?.closePopup();
        },
        click: () => {
          setPinned(true);
          markerRef.current?.openPopup();
        },
        popupclose: () => setPinned(false),
      }}
    >
      <ComplaintPopup complaint={complaint} onOpen={onOpenComplaint} />
    </CircleMarker>
  );
}

export const ComplaintMarkers = memo(function ComplaintMarkers({
  complaints,
  onOpenComplaint,
}: {
  complaints: Complaint[];
  onOpenComplaint: (id: number) => void;
}) {
  const handleClusterMouseOver = useCallback((e: L.LeafletMouseEvent) => {
    const cluster = e.layer as L.MarkerCluster;
    const count = cluster.getChildCount();
    cluster
      .bindTooltip(`${count} complaint${count === 1 ? "" : "s"} here — click to zoom in`, {
        direction: "top",
        offset: L.point(0, -28),
        className: "hp-tooltip",
      })
      .openTooltip();
  }, []);

  const handleClusterMouseOut = useCallback((e: L.LeafletMouseEvent) => {
    (e.layer as L.MarkerCluster).unbindTooltip();
  }, []);

  if (complaints.length === 0) return null;

  return (
    <MarkerClusterGroup
      chunkedLoading
      // Larger radius → fewer, bigger clusters at city zoom so the heatmap
      // stays the visual hero and the clusters read as density accents.
      maxClusterRadius={65}
      // Keep clustering active at city zoom levels; only dissolve into
      // individual markers at close range so thousands of complaints never
      // flood the React tree at once.
      disableClusteringAtZoom={15}
      showCoverageOnHover={false}
      spiderfyOnMaxZoom={true}
      iconCreateFunction={clusterIcon}
      onMouseOver={handleClusterMouseOver}
      onMouseOut={handleClusterMouseOut}
    >
      {complaints.map((c) => (
        <ComplaintMarker key={c.id} complaint={c} onOpenComplaint={onOpenComplaint} />
      ))}
    </MarkerClusterGroup>
  );
});
