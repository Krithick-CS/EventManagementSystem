const $ = (id) => document.getElementById(id);

function showMessage(el, text, isError) {
  el.textContent = text;
  el.className = isError ? "error" : "ok";
}

// ---------- Tabs ----------
document.querySelectorAll("nav [data-tab]").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll("nav [data-tab]").forEach((b) =>
      b.classList.toggle("active", b === btn));
    document.querySelectorAll(".tab").forEach((t) =>
      (t.hidden = t.id !== "tab-" + btn.dataset.tab));
    if (btn.dataset.tab === "events") loadEvents();
  });
});

function addCell(tr, value) {
  const td = document.createElement("td");
  td.textContent = value;
  tr.appendChild(td);
  return td;
}

// ---------- Venues ----------
async function loadVenues() {
  const res = await fetch("/api/venues");
  const venues = await res.json();

  const rows = $("venue-rows");
  rows.innerHTML = "";
  for (const v of venues) {
    const tr = document.createElement("tr");
    [v.id, v.name, v.capacity, v.location].forEach((x) => addCell(tr, x));
    rows.appendChild(tr);
  }

  const select = $("event-venue");
  select.innerHTML = "";
  for (const v of venues) {
    const opt = document.createElement("option");
    opt.value = v.id;
    opt.textContent = v.name + " (capacity " + v.capacity + ")";
    select.appendChild(opt);
  }
}

$("venue-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = e.target;
  const res = await fetch("/api/venues", {
    method: "POST",
    body: new URLSearchParams(new FormData(form)),
  });
  const data = await res.json();
  if (res.ok) {
    form.reset();
    showMessage($("message"), "Venue added.", false);
    loadVenues();
  } else {
    showMessage($("message"), data.error, true);
  }
});

// ---------- Events ----------
function formatTime(iso) {
  return iso.replace("T", " ");
}

async function setStatus(id, status) {
  const res = await fetch("/api/events/status", {
    method: "POST",
    body: new URLSearchParams({ id, status }),
  });
  const data = await res.json();
  if (res.ok) {
    showMessage($("event-action-message"),
      status === "APPROVED" ? "Event approved." : "Event cancelled.", false);
  } else {
    showMessage($("event-action-message"), data.error, true);
  }
  loadEvents();
}

function actionButton(label, onClick) {
  const b = document.createElement("button");
  b.type = "button";
  b.textContent = label;
  b.className = "small";
  b.addEventListener("click", onClick);
  return b;
}

async function loadEvents() {
  const res = await fetch("/api/events");
  const events = await res.json();
  const rows = $("event-rows");
  rows.innerHTML = "";

  if (events.length === 0) {
    const tr = document.createElement("tr");
    const td = addCell(tr, "No events yet.");
    td.colSpan = 8;
    rows.appendChild(tr);
    return;
  }

  for (const ev of events) {
    const tr = document.createElement("tr");
    addCell(tr, ev.id);
    addCell(tr, ev.title);
    addCell(tr, ev.venueName);
    addCell(tr, formatTime(ev.start));
    addCell(tr, formatTime(ev.end));
    addCell(tr, ev.registered + " / " + ev.capacity);

    const statusTd = document.createElement("td");
    const badge = document.createElement("span");
    badge.className = "badge " + ev.status.toLowerCase();
    badge.textContent = ev.status;
    statusTd.appendChild(badge);
    tr.appendChild(statusTd);

    const actionTd = document.createElement("td");
    if (ev.status === "DRAFT") {
      actionTd.appendChild(actionButton("Approve", () => setStatus(ev.id, "APPROVED")));
    }
    if (ev.status === "DRAFT" || ev.status === "APPROVED") {
      const cancel = actionButton("Cancel", () => setStatus(ev.id, "CANCELLED"));
      cancel.classList.add("danger");
      actionTd.appendChild(cancel);
    }
    tr.appendChild(actionTd);
    rows.appendChild(tr);
  }
}

$("event-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = e.target;
  const res = await fetch("/api/events", {
    method: "POST",
    body: new URLSearchParams(new FormData(form)),
  });
  const data = await res.json();
  if (res.ok) {
    form.reset();
    showMessage($("event-message"), "Event created as a draft.", false);
    loadEvents();
  } else {
    showMessage($("event-message"), data.error, true);
  }
});

loadVenues();