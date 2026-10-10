import Header from "@/components/Header"; 

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex h-screen flex-col overflow-hidden">
      <Header />
      <main className="min-h-0 flex-1">{children}</main>
    </div>
  );
}