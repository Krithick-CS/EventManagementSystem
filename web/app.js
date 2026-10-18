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
        if (btn.dataset.tab === "participants") loadParticipants();
        if (btn.dataset.tab === "registrations") loadRegistrationTab();
        if (btn.dataset.tab === "dashboard") loadDashboard();
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

// ---------- Participants ----------
async function loadParticipants() {
  const res = await fetch("/api/participants");
  const people = await res.json();

  const rows = $("participant-rows");
  rows.innerHTML = "";
  for (const p of people) {
    const tr = document.createElement("tr");
    [p.id, p.name, p.email].forEach((x) => addCell(tr, x));
    rows.appendChild(tr);
  }

  const select = $("reg-participant");
  const keep = select.value;
  select.innerHTML = "";
  for (const p of people) {
    const opt = document.createElement("option");
    opt.value = p.id;
    opt.textContent = p.name + " (" + p.email + ")";
    select.appendChild(opt);
  }
  if (keep) select.value = keep;
}

$("participant-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const form = e.target;
  const res = await fetch("/api/participants", {
    method: "POST",
    body: new URLSearchParams(new FormData(form)),
  });
  const data = await res.json();
  if (res.ok) {
    form.reset();
    showMessage($("participant-message"), "Participant added.", false);
    loadParticipants();
  } else {
    showMessage($("participant-message"), data.error, true);
  }
});

// ---------- Registrations ----------
async function loadRegistrationTab() {
  await loadParticipants();

  const res = await fetch("/api/events");
  const approved = (await res.json()).filter((e) => e.status === "APPROVED");

  for (const id of ["reg-event", "view-event"]) {
    const select = $(id);
    const keep = select.value;
    select.innerHTML = "";
    for (const ev of approved) {
      const opt = document.createElement("option");
      opt.value = ev.id;
      opt.textContent = ev.title + " (" + ev.registered + "/" + ev.capacity + ")";
      select.appendChild(opt);
    }
    if (keep) select.value = keep;
  }
  loadRegistered();
}

async function loadRegistered() {
  const rows = $("registered-rows");
  rows.innerHTML = "";
  const eventId = $("view-event").value;
  if (!eventId) return;

  const res = await fetch("/api/registrations?eventId=" + encodeURIComponent(eventId));
  const list = await res.json();

  if (list.length === 0) {
    const tr = document.createElement("tr");
    const td = addCell(tr, "No registrations yet.");
    td.colSpan = 4;
    rows.appendChild(tr);
    return;
  }
  for (const r of list) {
    const tr = document.createElement("tr");
    [r.id, r.name, r.email, r.registeredAt.substring(0, 16)].forEach((x) => addCell(tr, x));
    rows.appendChild(tr);
  }
}

$("view-event").addEventListener("change", loadRegistered);

$("register-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const res = await fetch("/api/registrations", {
    method: "POST",
    body: new URLSearchParams(new FormData(e.target)),
  });
  const data = await res.json();
  if (res.ok) {
    showMessage($("register-message"), "Registered successfully.", false);
    loadRegistrationTab();
  } else {
    showMessage($("register-message"), data.error, true);
  }
});

// ---------- Dashboard ----------
function statCard(label, value) {
  const div = document.createElement("div");
  div.className = "stat";
  const num = document.createElement("div");
  num.className = "stat-value";
  num.textContent = value;
  const lab = document.createElement("div");
  lab.className = "stat-label";
  lab.textContent = label;
  div.appendChild(num);
  div.appendChild(lab);
  return div;
}

function barRow(label, text, percent) {
  const row = document.createElement("div");
  row.className = "bar-row";

  const name = document.createElement("div");
  name.className = "bar-label";
  name.textContent = label;

  const track = document.createElement("div");
  track.className = "bar-track";
  const fill = document.createElement("div");
  fill.className = "bar-fill";
  fill.style.width = Math.min(100, Math.max(0, percent)) + "%";
  track.appendChild(fill);

  const value = document.createElement("div");
  value.className = "bar-text";
  value.textContent = text;

  row.appendChild(name);
  row.appendChild(track);
  row.appendChild(value);
  return row;
}

function emptyNote(text) {
  const p = document.createElement("p");
  p.className = "muted";
  p.textContent = text;
  return p;
}

async function loadDashboard() {
  const res = await fetch("/api/dashboard");
  const d = await res.json();

  const stats = $("stats");
  stats.innerHTML = "";
  stats.appendChild(statCard("Venues", d.venues));
  stats.appendChild(statCard("Participants", d.participants));
  stats.appendChild(statCard("Events (all)", d.events));
  stats.appendChild(statCard("Approved", d.approved));
  stats.appendChild(statCard("Draft", d.draft));
  stats.appendChild(statCard("Cancelled", d.cancelled));
  stats.appendChild(statCard("Active registrations", d.registrations));

  const eventBars = $("event-bars");
  eventBars.innerHTML = "";
  if (d.perEvent.length === 0) {
    eventBars.appendChild(emptyNote("No active events yet."));
  }
  for (const e of d.perEvent) {
    const pct = e.capacity > 0 ? (e.registered / e.capacity) * 100 : 0;
    eventBars.appendChild(
      barRow(e.title + " (" + e.status.toLowerCase() + ")",
             e.registered + " / " + e.capacity, pct));
  }

  const venueBars = $("venue-bars");
  venueBars.innerHTML = "";
  const maxHours = Math.max(1, ...d.perVenue.map((v) => parseFloat(v.hours)));
  for (const v of d.perVenue) {
    venueBars.appendChild(
      barRow(v.name, v.events + " event(s), " + v.hours + " h",
             (parseFloat(v.hours) / maxHours) * 100));
  }
}